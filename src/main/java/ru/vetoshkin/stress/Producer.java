package ru.vetoshkin.stress;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static ru.vetoshkin.stress.config.Configuration.Default.INFINITY_REQUEST;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class Producer implements Supplier<Request> {
    private static final Request postRequest = new RequestBuilder(HttpConstants.Methods.POST)
            .setUrl("http://localhost:8181/product/list/4/2")
            .build();

    private final AtomicInteger queryCount;
    private final boolean infinity;


    public Producer(int queryCount) {
        this.infinity = INFINITY_REQUEST == queryCount;
        this.queryCount = new AtomicInteger(queryCount);
    }


    @Override
    public Request get() {
        if (infinity)
            return next();

        int count = queryCount.decrementAndGet();
        if (count < 0)
            return null;

        return next();
    }


    public int getRemain() {
        if (infinity)
            return Integer.MAX_VALUE;

        return queryCount.get();
    }


    private Request next() {
        // TODO: ЗАПРОС
        return postRequest;
    }

}
