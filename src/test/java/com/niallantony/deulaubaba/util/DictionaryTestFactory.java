package com.niallantony.deulaubaba.util;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.dto.DictionaryPostRequest;
import com.niallantony.deulaubaba.dto.DictionaryPutRequest;


public class DictionaryTestFactory {
    public static DictionaryPostRequest createDictionaryPostRequest() {
        DictionaryPostRequest d = new DictionaryPostRequest();
        d.setStudentId("abc");
        d.setType(ExpressionType.BODY);
        d.setTitle("Title");
        d.getCategory().add(CommunicationCategoryLabel.PAIN);
        d.setDescription("Description");
        return d;
    }

    public static DictionaryPostRequest createBadRequest() {
        DictionaryPostRequest d = new DictionaryPostRequest();
        d.setStudentId("abc");
        return d;
    }

    public static DictionaryPutRequest createDictionaryPutRequest() {
        DictionaryPutRequest d = new DictionaryPutRequest();
        d.setId(1L);
        d.setStudentId("abc");
        d.setType(ExpressionType.BODY);
        d.setTitle("New Title");
        d.getCategory().add(CommunicationCategoryLabel.PAIN);
        d.getCategory().add(CommunicationCategoryLabel.SHOWME);
        d.setDescription("New Description");
        return d;
    }

    public static DictionaryPutRequest createBadPutReqeust() {
        DictionaryPutRequest d = new DictionaryPutRequest();
        d.setId(123L);
        return d;
    }
}
