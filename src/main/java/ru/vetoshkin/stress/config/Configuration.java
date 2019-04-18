package ru.vetoshkin.stress.config;
import lombok.Getter;
import lombok.Setter;
import ru.vetoshkin.stress.processor.ResponseProcessor;
import ru.vetoshkin.stress.producer.SendType;

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
    private int batchSize = Default.BATCH_SIZE;


    /**
     * Колиечество потоков
     */
    private int threads;


    /**
     * Роль в кластере
     */
    private Role role = Default.ROLE;


    /**
     * Таймаут
     */
    private int timeout = Default.TIMEOUT;


    /**
     * Количество повторов
     */
    private int retry = Default.RETRY;


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
    private List<String> servers = Default.SERVERS;


    /**
     * Вариант отправки
     */
    private SendType sendType = Default.SEND_TYPE;


    /**
     * Настройки по умолчанию
     */
    public static class Default {
        /**
         * Таймаут
         */
        public static final int TIMEOUT = 60_000;


        /**
         * Количество повторов
         */
        public static final int RETRY = 3;


        /**
         * По сколько вычитывать ответов
         */
        public static final int BATCH_SIZE = 100;


        /**
         * Groovy обработчик
         */
        public static final ResponseProcessor GROOVY_HANDLER = resp -> true;


        /**
         * Роль
         */
        public static final Role ROLE = Role.SINGLE;


        /**
         * Список серверов кластера
         */
        public static List<String> SERVERS = Collections.emptyList();


        /**
         * Неограниченныое количество запросов
         */
        public static int INFINITY_REQUEST = -1;


        /**
         * Тип отправки
         */
        public static final SendType SEND_TYPE = SendType.RANDOM;
    }
}
