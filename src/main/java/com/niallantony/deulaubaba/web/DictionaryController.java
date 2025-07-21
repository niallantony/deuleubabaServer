package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.DictionaryEntry;
import com.niallantony.deulaubaba.dto.DictionaryListingsResponse;
import com.niallantony.deulaubaba.dto.DictionaryPostRequest;
import com.niallantony.deulaubaba.services.DictionaryServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        if (dictionaryListingsResponse.getListings().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dictionaryListingsResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DictionaryEntry> addDictionary(@RequestBody DictionaryPostRequest dictionaryEntry) {
        return ResponseEntity.ok(dictionaryServices.addDictionaryEntry(dictionaryEntry));
    }
}
