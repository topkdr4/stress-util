package ru.vetoshkin.stress;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class RequestSupplier implements Supplier<Request> {
    private static final Request postRequest = new RequestBuilder(HttpConstants.Methods.POST)
            .setUrl("http://localhost:8181/product/list/4/2")
            .build();
    private final Context context;
    private final AtomicInteger queryCount;


    public RequestSupplier(Context context, int queryCount) {
        this.context = context;
        this.queryCount = new AtomicInteger(queryCount);
    }


    @Override
    public Request get() {
        int count = queryCount.decrementAndGet();
        if (count < 0)
            return null;

        // TODO:
        return postRequest;
    }


    public int getRemain() {
        return queryCount.get();
    }

}
