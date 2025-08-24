package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.dto.DictionaryEntryResponse;
import com.niallantony.deulaubaba.dto.DictionaryListingsResponse;
import com.niallantony.deulaubaba.dto.DictionaryPostRequest;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DictionaryServices {
    private final DictionaryRepository dictionaryRepository;
    private final StudentRepository studentRepository;
    private final CommunicationCategoryRepository communicationCategoryRepository;
    private final ObjectMapper jacksonObjectMapper;

    public DictionaryServices(DictionaryRepository dictionaryRepository, StudentRepository studentRepository, CommunicationCategoryRepository communicationCategoryRepository, ObjectMapper jacksonObjectMapper) {
        this.dictionaryRepository = dictionaryRepository;
        this.studentRepository = studentRepository;
        this.communicationCategoryRepository = communicationCategoryRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
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

    public DictionaryEntry addDictionaryEntry(String data) throws IOException {

        DictionaryPostRequest dictionaryPostRequest = jacksonObjectMapper.readValue(data, DictionaryPostRequest.class);

        Student student = studentRepository.findById(dictionaryPostRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        DictionaryEntry entry = new DictionaryEntry();
        entry.setStudent(student);
        entry.setTitle(dictionaryPostRequest.getTitle());
        entry.setDescription(dictionaryPostRequest.getDescription());
        entry.setType(dictionaryPostRequest.getType());
        Set<CommunicationCategory> category = dictionaryPostRequest.getCategory().stream()
                .map(label -> communicationCategoryRepository.findByLabel(label)
                        .orElseThrow(() -> new ResourceNotFoundException("Category Doesn't Exist")))
                .collect(Collectors.toSet());
        entry.setCategory(category);
        dictionaryRepository.save(entry);
        return entry;
    }
    public DictionaryEntry addDictionaryEntryWithImage(String data, MultipartFile imageFile) throws IOException {

        DictionaryPostRequest dictionaryPostRequest = jacksonObjectMapper.readValue(data, DictionaryPostRequest.class);
        String filename = UUID.randomUUID() + "-" + imageFile.getOriginalFilename();
        Path path = Paths.get("uploads", filename);
        Files.createDirectories(path.getParent());
        imageFile.transferTo(path);

        Student student = studentRepository.findById(dictionaryPostRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        DictionaryEntry entry = new DictionaryEntry();
        entry.setStudent(student);
        entry.setTitle(dictionaryPostRequest.getTitle());
        entry.setDescription(dictionaryPostRequest.getDescription());
        entry.setType(dictionaryPostRequest.getType());
        entry.setImgSrc(filename);
        Set<CommunicationCategory> category = dictionaryPostRequest.getCategory().stream()
                .map(label -> communicationCategoryRepository.findByLabel(label)
                        .orElseThrow(() -> new ResourceNotFoundException("Category Doesn't Exist")))
                .collect(Collectors.toSet());
        entry.setCategory(category);
        dictionaryRepository.save(entry);
        return entry;
    }
}
