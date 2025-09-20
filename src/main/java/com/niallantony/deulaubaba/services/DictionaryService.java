package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DictionaryService {
    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
    private final DictionaryRepository dictionaryRepository;
    private final CommunicationCategoryRepository communicationCategoryRepository;
    private final ObjectMapper jacksonObjectMapper;
    private final FileStorageService fileStorageService;
    private final StudentService studentService;

    public DictionaryService(
            DictionaryRepository dictionaryRepository,
            CommunicationCategoryRepository communicationCategoryRepository,
            ObjectMapper jacksonObjectMapper,
            FileStorageService fileStorageService,
            StudentService studentService) {
        this.dictionaryRepository = dictionaryRepository;
        this.communicationCategoryRepository = communicationCategoryRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
        this.studentService = studentService;
    }

    public DictionaryListingsResponse getDictionaryListings(String studentId, String userId) {
       Student student = studentService.getAuthorisedStudent(studentId, userId);
       DictionaryListingsResponse response = new DictionaryListingsResponse();
       List<DictionaryEntry> listings = dictionaryRepository.findAllByStudent(student);
       if (listings.isEmpty()) {
           return response;
       }
       Set<ExpressionType> types = getExpressionTypes(listings);
       List<DictionaryEntryResponse> listingsDTO = listings.stream()
               .map(listing -> new DictionaryEntryResponse(
                       listing.getId(),
                       listing.getType(),
                       listing.getTitle(),
                       listing.getCategory().stream()
                               .map(CommunicationCategory::getLabel)
                               .collect(Collectors.toSet()),
                       listing.getImgSrc(),
                       listing.getDescription()

               )).toList();
       response.setListings(listingsDTO);
       response.setExpressiontypes(types);
       return response;
    }

    private Set<ExpressionType> getExpressionTypes(List<DictionaryEntry> dictionaryEntries) {
        return dictionaryEntries.stream()
                .map(DictionaryEntry::getType)
                .collect(Collectors.toSet());
    }

    @Transactional
    public DictionaryEntry addDictionaryEntry(String data, MultipartFile imageFile, String userId) throws IOException {
        DictionaryPostRequest dictionaryPostRequest = jacksonObjectMapper.readValue(data, DictionaryPostRequest.class);
        Student student = studentService.getAuthorisedStudent(dictionaryPostRequest.getStudentId(), userId);
        DictionaryEntry entry = new DictionaryEntry();
        if (imageFile != null) {
            String filename = fileStorageService.storeImage(imageFile);
            entry.setImgSrc(filename);
        }
        entry.setStudent(student);
        assignChanges(entry, dictionaryPostRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public DictionaryEntry updateDictionaryEntry(String data, MultipartFile image, String userId) throws IOException {
        DictionaryPutRequest dictionaryPutRequest = jacksonObjectMapper.readValue(data, DictionaryPutRequest.class);
        if (!studentService.studentBelongsToUser(dictionaryPutRequest.getStudentId(), userId)) {
            throw new UserNotAuthorizedException("User Not Authorized");
        }
        DictionaryEntry entry = dictionaryRepository.findById(dictionaryPutRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Entry Not Found"));
        if (image != null) {
            try {
                String filename = fileStorageService.storeImage(image);
                fileStorageService.deleteImage(entry);
                entry.setImgSrc(filename);
            } catch (FileStorageException e) {
                log.warn("Image failed to save into dictionary", e);
            }
        }
        assignChanges(entry, dictionaryPutRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public void deleteDictionaryEntry(String id, String userId) throws ResourceNotFoundException {
        Long longId = Long.parseLong(id);
        if (!dictionaryRepository.existsById(longId)) {
            throw new ResourceNotFoundException("Dictionary Not Found");
        }
        DictionaryEntry entry = dictionaryRepository.getReferenceById(longId);
        if (!studentService.studentBelongsToUser(entry.getStudent().getStudentId(), userId)) {
            throw new UserNotAuthorizedException("User Not Authorized");
        }
        if (entry.getImgSrc() != null) {
            fileStorageService.deleteImage(entry);
        }
        dictionaryRepository.deleteById(longId);
    }

    private void assignChanges (DictionaryEntry entry, DictionaryRequest dictionaryPostRequest) {
        entry.setTitle(dictionaryPostRequest.getTitle());
        entry.setDescription(dictionaryPostRequest.getDescription());
        entry.setType(dictionaryPostRequest.getType());
        Set<CommunicationCategory> category = dictionaryPostRequest.getCategory().stream()
                .map(label -> communicationCategoryRepository.findByLabel(label)
                        .orElseThrow(() -> new ResourceNotFoundException("Category Doesn't Exist")))
                .collect(Collectors.toSet());
        entry.setCategory(category);
    }

}
