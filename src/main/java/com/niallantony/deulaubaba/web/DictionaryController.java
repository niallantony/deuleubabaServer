package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.dto.DictionaryListingsResponse;
import com.niallantony.deulaubaba.services.DictionaryServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping(path = "/dictionary", produces = "application/json")
public class DictionaryController {

    private final DictionaryServices dictionaryServices;

    @Autowired
    public DictionaryController(DictionaryServices dictionaryServices) {
        this.dictionaryServices = dictionaryServices;
    }

    @GetMapping
    public ResponseEntity<DictionaryListingsResponse> getDictionaryListings(@RequestParam String student_id) {
        DictionaryListingsResponse dictionaryListingsResponse = dictionaryServices.getDictionaryListings(student_id);
        if (dictionaryListingsResponse.getListings() == null || dictionaryListingsResponse.getListings().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dictionaryListingsResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DictionaryEntry> addDictionary(
            @RequestPart("data") String dictionaryEntry,
            @RequestPart(value = "image", required = false)MultipartFile imageFile ) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
           return ResponseEntity.ok(dictionaryServices.addDictionaryEntry(dictionaryEntry));
        }
        return ResponseEntity.ok(dictionaryServices.addDictionaryEntryWithImage(dictionaryEntry, imageFile));
    }
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudent(
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null) {
            return ResponseEntity.ok(dictionaryServices.updateDictionaryEntry(request, image));
        }
        return ResponseEntity.ok(dictionaryServices.updateDictionaryEntry(request));
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        dictionaryServices.deleteDictionaryEntry(id);
        return ResponseEntity.noContent().build();
    }

}
