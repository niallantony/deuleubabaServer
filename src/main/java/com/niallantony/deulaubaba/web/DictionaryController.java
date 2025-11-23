package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryListingsResponse;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryPostRequest;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryPutRequest;
import com.niallantony.deulaubaba.exceptions.InvalidDictionaryDataException;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.DictionaryService;
import com.niallantony.deulaubaba.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Slf4j
@RequestMapping(path = "/dictionary", produces = "application/json")
public class DictionaryController {

    private final DictionaryService dictionaryService;
    private final JsonUtils jsonUtils;

    @Autowired
    public DictionaryController(DictionaryService dictionaryService, JsonUtils jsonUtils) {
        this.dictionaryService = dictionaryService;
        this.jsonUtils = jsonUtils;
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
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false)MultipartFile imageFile,
            @CurrentUser String userId
    )  {
        DictionaryPostRequest request = jsonUtils.parse(
                data,
                DictionaryPostRequest.class,
                () -> new InvalidDictionaryDataException("Entry data not valid")
        );
        DictionaryEntry entry = dictionaryService.addDictionaryEntry(request, imageFile, userId);
        return ResponseEntity.created(
                                     URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/dictionary").toUriString() + "/" + entry.getId())
                             )
                             .build();
    }

    @PutMapping
    public ResponseEntity<?> updateDictionary(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @CurrentUser String userId
    )  {
        DictionaryPutRequest request = jsonUtils.parse(
                data,
                DictionaryPutRequest.class,
                () -> new InvalidDictionaryDataException("Entry data not valid")
        );
        dictionaryService.updateDictionaryEntry(request, image, userId);
        return ResponseEntity.noContent()
                             .location(
                                     URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/dictionary").toUriString() + "/" + request.getId())
                             )
                             .build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteDictionary(
            @PathVariable String id,
            @CurrentUser String userId
    ) {
        try {
            Long longId = Long.parseLong(id);
            dictionaryService.deleteDictionaryEntry(longId, userId);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            throw new InvalidDictionaryDataException("Entry Id not valid");
        }
    }

}
