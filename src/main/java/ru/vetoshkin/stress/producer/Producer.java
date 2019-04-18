package ru.vetoshkin.stress.producer;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;
import ru.vetoshkin.stress.StressConfig;
import ru.vetoshkin.stress.util.Json;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static ru.vetoshkin.stress.config.Configuration.Default.INFINITY_REQUEST;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Slf4j
public class Producer implements Supplier<Request> {
    private static final Request postRequest = new RequestBuilder(HttpConstants.Methods.POST)
            .setUrl("http://localhost:8181/product/list/4/2")
            .build();

    private final AtomicInteger queryCount;
    private final boolean infinity;

    private final RequestFactory factory;

    public Producer(StressConfig config) {
        int queryCount  = config.getRequestCount();

        this.infinity   = INFINITY_REQUEST == queryCount;
        this.queryCount = new AtomicInteger(queryCount);
        this.factory    = new RequestFactory(config);
        this.factory.init();
    }


    @Override
    public Request get() {
        if (infinity)
            return factory.next();

        int count = queryCount.decrementAndGet();
        if (count < 0)
            return null;

        return factory.next();
    }


    private class RequestFactory {
        private final SendType sendType;
        private final String requestFile;
        private List<ru.vetoshkin.stress.producer.Request> allRequests = Collections.emptyList();


        private RequestFactory(StressConfig config) {
            this.sendType    = config.getSendType();
            this.requestFile = config.getRequestFile();
        }


        private void init () {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(requestFile), StandardCharsets.UTF_8)) {
                String line;
                List<ru.vetoshkin.stress.producer.Request> list = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    ru.vetoshkin.stress.producer.Request request = Json.getJsonMapper().readValue(line, ru.vetoshkin.stress.producer.Request.class);
                    request.check();
                    list.add(request);
                }

                if (list.isEmpty())
                    throw new IllegalArgumentException("request file is empty");

                allRequests = Collections.unmodifiableList(list);
            } catch (Exception e) {
                log.error("request file error: {}", e);
            }
        }


        private final AtomicInteger position = new AtomicInteger(-1);


        private Request next() {
            switch (sendType) {
                case RANDOM:
                    int randomPos = ThreadLocalRandom.current().nextInt(0, allRequests.size());
                    return get(randomPos);

                case CONSISTENTLY:
                    int consPos = position.updateAndGet(operand -> {
                        int pos = operand + 1;
                        if (pos > allRequests.size())
                            return 0;

                        return pos;
                    });
                    return get(consPos);
            }

            throw new IllegalArgumentException("Incorrect param `sendType`");
        }


        private Request get(int pos) {
            ru.vetoshkin.stress.producer.Request request = allRequests.get(pos);

            RequestBuilder builder = new RequestBuilder(request.getMethod());

            for (String k : request.getHeaders().keySet()) {
                builder = builder.addHeader(k, request.getHeaders().get(k));
            }

            return builder.setBody(request.getBody())
                    .setUrl(request.getUrl())
                    .build();
        }
    }

}
