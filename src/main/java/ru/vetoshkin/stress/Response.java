package ru.vetoshkin.stress;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Getter
@Setter
public class Response {
    /**
     * Тело ответа
     */
    private byte[] body;

    /**
     * Была ли ошибка
     */
    private boolean transportError;

    /**
     * Начало запроса
     */
    private long start;

    /**
     * Конец запроса
     */
    private long end;

    /**
     * HTTP status
     */
    private int httpStatusCode;

    /**
     * Заголовки ответа
     */
    private HttpHeaders responseHeaders;

    /**
     * Успешный ответ
     */
    private boolean success;


    public long getDiffTime() {
        return end - start;
    }

}
