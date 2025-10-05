package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.dto.dictionary.*;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.exceptions.InvalidDictionaryDataException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.DictionaryMapper;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DictionaryService {
    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
    private final DictionaryRepository dictionaryRepository;
    private final CommunicationCategoryRepository communicationCategoryRepository;
    private final JsonUtils jsonUtils;
    private final FileStorageService fileStorageService;
    private final StudentService studentService;
    private final DictionaryMapper dictionaryMapper;

    public DictionaryService(
            DictionaryRepository dictionaryRepository,
            CommunicationCategoryRepository communicationCategoryRepository,
            FileStorageService fileStorageService,
            StudentService studentService,
            DictionaryMapper dictionaryMapper,
            JsonUtils jsonUtils
    ) {
        this.dictionaryRepository = dictionaryRepository;
        this.communicationCategoryRepository = communicationCategoryRepository;
        this.jsonUtils = jsonUtils;
        this.fileStorageService = fileStorageService;
        this.studentService = studentService;
        this.dictionaryMapper = dictionaryMapper;
    }

    @Transactional
    public DictionaryListingsResponse getDictionaryListings(String studentId, String userId) {
       Student student = studentService.getAuthorisedStudent(studentId, userId);
       DictionaryListingsResponse response = new DictionaryListingsResponse();
       List<DictionaryEntry> listings = dictionaryRepository.findAllByStudent(student);
       if (listings.isEmpty()) {
           return response;
       }
       Set<ExpressionType> types = getExpressionTypes(listings);
       List<DictionaryEntryDTO> listingsDTO = listings.stream()
                                                      .map(dictionaryMapper::entityToDto)
                                                      .toList();
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
    public DictionaryEntry addDictionaryEntry(String data, MultipartFile imageFile, String userId) {
        DictionaryPostRequest dictionaryPostRequest = jsonUtils.parse(
                data,
                DictionaryPostRequest.class,
                () -> new InvalidDictionaryDataException("Entry data not valid")
        );
        validateDictionaryRequest(dictionaryPostRequest);
        Student student = studentService.getAuthorisedStudent(dictionaryPostRequest.getStudentId(), userId);
        DictionaryEntry entry = new DictionaryEntry();
        entry.setStudent(student);
        if (imageFile != null) {
            try {
                String filename = fileStorageService.storeImage(imageFile);
                entry.setImgsrc(filename);
            } catch (FileStorageException e) {
                log.warn("File not saved: ", e);
            }
        }
        assignChanges(entry, dictionaryPostRequest);
        dictionaryRepository.save(entry);
        return entry;
    }

    @Transactional
    public DictionaryEntry updateDictionaryEntry(String data, MultipartFile image, String userId) {
        String oldFilename = null;
        DictionaryPutRequest dictionaryPutRequest = jsonUtils.parse(
                data,
                DictionaryPutRequest.class,
                () -> new InvalidDictionaryDataException("Entry data not valid")
        );
        if (dictionaryPutRequest.getId() == null) {
            throw new InvalidDictionaryDataException("Entry data not valid");
        }
        validateDictionaryRequest(dictionaryPutRequest);
        if (!studentService.studentBelongsToUser(dictionaryPutRequest.getStudentId(), userId)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        DictionaryEntry entry = dictionaryRepository.findById(dictionaryPutRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found"));
        if (image != null) {
            try {
                String filename = fileStorageService.storeImage(image);
                oldFilename = entry.getImgsrc();
                entry.setImgsrc(filename);
            } catch (FileStorageException e) {
                log.warn("Image failed to save into dictionary", e);
            }
        }
        assignChanges(entry, dictionaryPutRequest);
        dictionaryRepository.save(entry);
        if (oldFilename != null) {
            fileStorageService.deleteImage(oldFilename);
        }
        return entry;
    }

    @Transactional
    public void deleteDictionaryEntry(String id, String userId) {
        Long longId = Long.parseLong(id);
        if (!dictionaryRepository.existsById(longId)) {
            throw new ResourceNotFoundException("Dictionary not found");
        }
        DictionaryEntry entry = dictionaryRepository.getReferenceById(longId);
        if (!studentService.studentBelongsToUser(entry.getStudent().getStudentId(), userId)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        if (entry.getImgsrc() != null) {
            try {
                fileStorageService.deleteImage(entry.getImgsrc());
            } catch (FileStorageException e) {
                log.warn("Image wasn't deleted");
                log.warn(entry.getImgsrc());
            }
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

    private void validateDictionaryRequest(DictionaryRequest dictionaryRequest) {
       if (
               dictionaryRequest.getTitle() == null
               || dictionaryRequest.getStudentId() == null
               || dictionaryRequest.getCategory().isEmpty()
               || dictionaryRequest.getType() == null
       ) {
           throw new InvalidDictionaryDataException("Entry data not valid");
       }
    }
}
