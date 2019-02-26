package ru.vetoshkin.stress;
import groovy.lang.GroovyClassLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.util.MiscUtils;
import ru.vetoshkin.stress.config.Configuration;
import ru.vetoshkin.stress.config.Role;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;





@Getter
@AllArgsConstructor
public class StressConfig {

    /**
     * Конфигурация Http клиента
     */
    private final AsyncHttpClientConfig httpClientConfig;


    /**
     * Количество отправляемых запросов
     */
    private final int requestCount;


    /**
     * По сколько вычитывать ответов
     */
    private final int batchSize;


    /**
     * Колиечество потоков
     */
    private final int threads;


    /**
     * Роль в кластере
     */
    private final Role role;


    /**
     * Время окончания теста
     */
    private final Date endDate;


    /**
     * Время начада теста
     */
    private final Date startDate;


    /**
     * Groovy обработчик
     */
    private final ResponseProcessor groovyHandler;
    private final String groovyHandlerName;


    /**
     * Файл запросов
     */
    private final String requestFile;


    /**
     * Список серверов кластера
     */
    private final List<String> servers;


    public static StressConfig build(Configuration conf) {
        if (conf == null)
            throw new IllegalArgumentException("configuration not defined");

        int threads = conf.getThreads();
        if (threads <= 0)
            throw new IllegalArgumentException("param `threads` not defined");

        int timeout = conf.getTimeout();
        if (timeout < 0)
            throw new IllegalArgumentException("param `timeout` not defined");

        int retry = conf.getRetry();
        if (retry < 0)
            throw new IllegalArgumentException("param `retry` not defined");

        int requestCount = conf.getRequestCount();
        if (requestCount < -1 || requestCount == 0)
            throw new IllegalArgumentException("param `requestCount` not defined");

        int batchSize = conf.getBatchSize();
        if (batchSize <= 0)
            throw new IllegalArgumentException("param `batchSize` not defined");

        String groovyHandlerName = conf.getGroovyHandler();
        ResponseProcessor groovyHandler;
        if (groovyHandlerName != null) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(groovyHandlerName));
                String sourceClass = new String(bytes, StandardCharsets.UTF_8);

                GroovyClassLoader groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
                groovyHandler = (ResponseProcessor) groovyClassLoader.parseClass(sourceClass).newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("param `groovyHandler` error: " + e.getMessage());
            }
        } else {
            groovyHandlerName = "default";
            groovyHandler = Configuration.Default.GROOVY_HANDLER;
        }

        Role role = conf.getRole();
        if (role != Role.SINGLE)
            throw new IllegalArgumentException("param `role` should be only SINGLE");


        AsyncHttpClientConfig httpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setIoThreadsCount(threads)
                .setMaxConnections(threads)
                .setConnectTimeout(timeout)
                .setMaxRequestRetry(retry)
                .build();

        return new StressConfig(
                httpClientConfig,
                requestCount,
                batchSize,
                threads,
                role,
                conf.getEndDate(),
                conf.getStartDate(),
                groovyHandler,
                groovyHandlerName,
                conf.getRequestFile(),
                conf.getServers()
        );
    }


    public void printConfig() {
        System.out.println(""
                + "#     ____ ______ ___   ____ ____ ____     ______ ____   ____   __ \n"
                + "#    / __//_  __// _ \\ / __// __// __/____/_  __// __ \\ / __ \\ / / \n"
                + "#   _\\ \\   / /  / , _// _/ _\\ \\ _\\ \\ /___/ / /  / /_/ // /_/ // /__\n"
                + "#  /___/  /_/  /_/|_|/___//___//___/      /_/   \\____/ \\____//____/\n"
                + "#                                                                  "
                + "\n\n");

        String[][] configs = {
                { "requestCount", String.valueOf(requestCount == Configuration.Default.INFINITY_REQUEST ? Double.POSITIVE_INFINITY : requestCount) },
                { "batchSize", String.valueOf(batchSize) },
                { "threads", String.valueOf(threads) },
                { "role", String.valueOf(role) },
                { "timeout", String.valueOf(httpClientConfig.getConnectTimeout()) },
                { "retry", String.valueOf(httpClientConfig.getMaxRequestRetry()) },
                { "startDate", String.valueOf(startDate) },
                { "endDate", String.valueOf(endDate) },
                { "groovyHandler", String.valueOf(groovyHandlerName) },
                { "requestFile", String.valueOf(requestFile) }
        };

        final int symbolCount = 70;
        final String symbol = ".";
        final String delimiter = "";


        StringBuilder configBuilder = new StringBuilder();
        configBuilder.append("Configuration:\n");

        for (String[] config : configs) {
            int length = config[0].length() + config[1].length();
            int copies = symbolCount - length;

            configBuilder.append("\t")
                    .append(config[0])
                    .append(String.join(delimiter, Collections.nCopies(copies, symbol)))
                    .append(config[1]).append("\n");
        }

        System.out.println(configBuilder);
    }
}
