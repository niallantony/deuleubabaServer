package com.niallantony.deulaubaba.web;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@Slf4j
@RequestMapping(path = "/dictionary", produces = "application/json")
public class DictionaryController {

    public DictionaryRepository dictionaryRepository;
    public StudentRepository studentRepository;
    public CommunicationCategoryRepository communicationCategoryRepository;

    @Autowired
    public DictionaryController(DictionaryRepository dictionaryRepository, StudentRepository studentRepository, CommunicationCategoryRepository communicationCategoryRepository) {
        this.dictionaryRepository = dictionaryRepository;
        this.studentRepository = studentRepository;
        this.communicationCategoryRepository = communicationCategoryRepository;
    }

    @GetMapping
    public ResponseEntity<DictionaryListingsResponse> getDictionaryListings(@RequestParam String student_id) {
        Student student = studentRepository.findById(student_id).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        DictionaryListingsResponse response = new DictionaryListingsResponse();
        List<DictionaryEntry> listings = dictionaryRepository.findAllByStudent(student);
        if (listings.isEmpty()) {
            return ResponseEntity.noContent().build();
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
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DictionaryEntry> addDictionary(@RequestBody DictionaryPostRequest dictionaryEntry) {
        Student student = studentRepository.findById(dictionaryEntry.getStudentId()).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        DictionaryEntry newEntry = new DictionaryEntry();
        log.info("Adding new dictionary entry: " + dictionaryEntry);
        log.info(String.valueOf(dictionaryEntry.getCategory().getClass()));
        newEntry.setStudent(student);
        newEntry.setTitle(dictionaryEntry.getTitle());
        newEntry.setDescription(dictionaryEntry.getDescription());
        newEntry.setType(dictionaryEntry.getType());
        newEntry.setImgSrc(dictionaryEntry.getImgsrc());
        Set<CommunicationCategory> category = dictionaryEntry.getCategory().stream()
                        .map(label -> communicationCategoryRepository.findByLabel(label).orElseThrow())
                        .collect(Collectors.toSet());
        log.info(String.valueOf(category));
        newEntry.setCategory(category);
        dictionaryRepository.save(newEntry);

        return ResponseEntity.ok(newEntry);
    }
}
