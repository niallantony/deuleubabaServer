package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryListingsResponse;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// TODO: Move JSONUTILS into Controller layer
@RestController
@Slf4j
@RequestMapping(path = "/dictionary", produces = "application/json")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @Autowired
    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping
    public ResponseEntity<DictionaryListingsResponse> getDictionaryListings(
            @RequestParam String student_id,
            @CurrentUser String userId
    ) {
        DictionaryListingsResponse dictionaryListingsResponse = dictionaryService.getDictionaryListings(student_id, userId);
        if (dictionaryListingsResponse.getListings() == null || dictionaryListingsResponse.getListings().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dictionaryListingsResponse);
    }

    @PostMapping
    public ResponseEntity<DictionaryEntry> addDictionary(
            @RequestPart("data") String dictionaryEntry,
            @RequestPart(value = "image", required = false)MultipartFile imageFile,
            @CurrentUser String userId
    )  {
        DictionaryEntry entry = dictionaryService.addDictionaryEntry(dictionaryEntry, imageFile, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PutMapping
    public ResponseEntity<?> updateDictionary(
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @CurrentUser String userId
    )  {
        DictionaryEntry entry = dictionaryService.updateDictionaryEntry(request, image, userId);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteDictionary(
            @PathVariable String id,
            @CurrentUser String userId
    ) {
        dictionaryService.deleteDictionaryEntry(id, userId);
        return ResponseEntity.noContent().build();
    }

}
