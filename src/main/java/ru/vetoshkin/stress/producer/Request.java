package ru.vetoshkin.stress.producer;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Getter
public class Request {
    private String method;
    private String url;
    private String body;
    private Map<String, String> headers;


    public void check() {
        if (isEmpty(method))
            throw new IllegalStateException("Incorrect http method");

        if (isEmpty(url))
            throw new IllegalStateException("Incorrect url");

        if (headers == null)
            headers = Collections.emptyMap();
    }


    private static boolean isEmpty(String source) {
        return source == null || source.trim().equals("");
    }
}
