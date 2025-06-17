package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/dictionary", produces = "application/json")
public class DictionaryController {

    public DictionaryRepository dictionaryRepository;

    @Autowired
    public DictionaryController(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }
}
