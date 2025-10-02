package com.niallantony.deulaubaba.util;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.dto.DictionaryPostRequest;

public class DictionaryTestFactory {
    public static DictionaryPostRequest createDictionaryPostRequest() {
        DictionaryPostRequest d = new DictionaryPostRequest();
        d.setStudentId("abc");
        d.setType(ExpressionType.BODY);
        d.setTitle("Title");
        d.getCategory().add(CommunicationCategoryLabel.PAIN);
        d.setImgsrc("example.jpg");
        d.setDescription("Description");
        return d;
    }

    public static DictionaryPostRequest createBadRequest() {
        DictionaryPostRequest d = new DictionaryPostRequest();
        d.setStudentId("abc");
        return d;
    }
}
