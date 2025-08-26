package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
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
public class DictionaryServices {
    private static final Logger log = LoggerFactory.getLogger(DictionaryServices.class);
    private final DictionaryRepository dictionaryRepository;
    private final StudentRepository studentRepository;
    private final CommunicationCategoryRepository communicationCategoryRepository;
    private final ObjectMapper jacksonObjectMapper;
    private final FileStorageService fileStorageService;

    public DictionaryServices(
            DictionaryRepository dictionaryRepository,
            StudentRepository studentRepository,
            CommunicationCategoryRepository communicationCategoryRepository,
            ObjectMapper jacksonObjectMapper,
            FileStorageService fileStorageService
    ) {
        this.dictionaryRepository = dictionaryRepository;
        this.studentRepository = studentRepository;
        this.communicationCategoryRepository = communicationCategoryRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
    }

    public DictionaryListingsResponse getDictionaryListings(String studentId) {
       Student student = studentRepository.findById(studentId)
               .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
       DictionaryListingsResponse response = new DictionaryListingsResponse();
       List<DictionaryEntry> listings = dictionaryRepository.findAllByStudent(student);
       if (listings.isEmpty()) {
           return response;
       }
       Set<ExpressionType> types = listings.stream()
               .map(DictionaryEntry::getType)
               .collect(Collectors.toSet());
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

    @Transactional
    public DictionaryEntry addDictionaryEntry(String data) throws IOException {

        DictionaryPostRequest dictionaryPostRequest = jacksonObjectMapper.readValue(data, DictionaryPostRequest.class);

        Student student = studentRepository.findById(dictionaryPostRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));

        DictionaryEntry entry = new DictionaryEntry();
        entry.setStudent(student);
        assignChanges(entry, dictionaryPostRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public DictionaryEntry addDictionaryEntryWithImage(String data, MultipartFile imageFile) throws IOException {

        DictionaryPostRequest dictionaryPostRequest = jacksonObjectMapper.readValue(data, DictionaryPostRequest.class);
        String filename = fileStorageService.storeImage(imageFile);

        Student student = studentRepository.findById(dictionaryPostRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));

        DictionaryEntry entry = new DictionaryEntry();
        entry.setStudent(student);
        entry.setImgSrc(filename);
        assignChanges(entry, dictionaryPostRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public DictionaryEntry updateDictionaryEntry(String data) throws IOException {
        DictionaryPutRequest dictionaryPutRequest = jacksonObjectMapper.readValue(data, DictionaryPutRequest.class);

        DictionaryEntry entry = dictionaryRepository.findById(dictionaryPutRequest.getId()).orElseThrow(() -> new ResourceNotFoundException("Entry Not Found"));
        assignChanges(entry, dictionaryPutRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public DictionaryEntry updateDictionaryEntry(String data, MultipartFile image) throws IOException {
        DictionaryPutRequest dictionaryPutRequest = jacksonObjectMapper.readValue(data, DictionaryPutRequest.class);
        DictionaryEntry entry = dictionaryRepository.findById(dictionaryPutRequest.getId()).orElseThrow(() -> new ResourceNotFoundException("Entry Not Found"));
        try {
            String filename = fileStorageService.storeImage(image);
            fileStorageService.deleteImage(entry);
            entry.setImgSrc(filename);
        } catch (IOException e) {
            log.warn("Image failed to save into dictionary", e);
        }
        assignChanges(entry, dictionaryPutRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public void deleteDictionaryEntry(String id) throws ResourceNotFoundException {
        Long longId = Long.parseLong(id);
        if (!dictionaryRepository.existsById(longId)) {
            throw new ResourceNotFoundException("Dictionary Not Found");
        }
        DictionaryEntry entry = dictionaryRepository.getReferenceById(longId);
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
