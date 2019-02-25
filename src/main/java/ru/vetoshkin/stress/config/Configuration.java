package ru.vetoshkin.stress.config;
import lombok.Getter;
import lombok.Setter;
import ru.vetoshkin.stress.ResponseProcessor;

import java.util.Collections;
import java.util.Date;
import java.util.List;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Getter
@Setter
public class Configuration {
    /**
     * Количество отправляемых запросов
     */
    private int requestCount;


    /**
     * По сколько вычитывать ответов
     */
    private int batchSize;


    /**
     * Колиечество потоков
     */
    private int threads;


    /**
     * Роль в кластере
     */
    private Role role;


    /**
     * Таймаут
     */
    private int timeout;


    /**
     * Количество повторов
     */
    private int retry;


    /**
     * Время окончания теста
     */
    private Date endDate;


    /**
     * Время начада теста
     */
    private Date startDate;


    /**
     * Groovy обработчик
     */
    private String groovyHandler;


    /**
     * Файл запросов
     */
    private String requestFile;


    /**
     * Список серверов кластера
     */
    private List<String> servers;



    /**
     * Настройки по умолчанию
     */
    public static class Default {
        /**
         * Таймаут
         */
        public static int TIMEOUT = 60_000;


        /**
         * Количество повторов
         */
        public static int RETRY = 3;


        /**
         * Groovy обработчик
         */
        public static ResponseProcessor GROOVY_HANDLER = resp -> true;


        /**
         * Роль
         */
        public static Role ROLE = Role.SINGLE;


        /**
         * Список серверов кластера
         */
        public static List<String> SERVERS = Collections.emptyList();


        /**
         * Неограниченныое количество запросов
         */
        public static int INFINITY_REQUEST = -1;
    }
}
