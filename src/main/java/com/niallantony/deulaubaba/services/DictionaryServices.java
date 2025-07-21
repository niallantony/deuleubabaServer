package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.CommunicationCategory;
import com.niallantony.deulaubaba.DictionaryEntry;
import com.niallantony.deulaubaba.ExpressionType;
import com.niallantony.deulaubaba.Student;
import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.dto.DictionaryEntryResponse;
import com.niallantony.deulaubaba.dto.DictionaryListingsResponse;
import com.niallantony.deulaubaba.dto.DictionaryPostRequest;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DictionaryServices {
    private final DictionaryRepository dictionaryRepository;
    private final StudentRepository studentRepository;
    private final CommunicationCategoryRepository communicationCategoryRepository;

    public DictionaryServices(DictionaryRepository dictionaryRepository, StudentRepository studentRepository, CommunicationCategoryRepository communicationCategoryRepository) {
        this.dictionaryRepository = dictionaryRepository;
        this.studentRepository = studentRepository;
        this.communicationCategoryRepository = communicationCategoryRepository;
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

    public DictionaryEntry addDictionaryEntry(DictionaryPostRequest dictionaryPostRequest) {
        Student student = studentRepository.findById(dictionaryPostRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        DictionaryEntry entry = new DictionaryEntry();
        entry.setStudent(student);
        entry.setTitle(dictionaryPostRequest.getTitle());
        entry.setDescription(dictionaryPostRequest.getDescription());
        entry.setType(dictionaryPostRequest.getType());
        entry.setImgSrc(dictionaryPostRequest.getImgsrc());
        Set<CommunicationCategory> category = dictionaryPostRequest.getCategory().stream()
                .map(label -> communicationCategoryRepository.findByLabel(label)
                        .orElseThrow(() -> new ResourceNotFoundException("Category Doesn't Exist")))
                .collect(Collectors.toSet());
        entry.setCategory(category);
        dictionaryRepository.save(entry);
        return entry;
    }
}
