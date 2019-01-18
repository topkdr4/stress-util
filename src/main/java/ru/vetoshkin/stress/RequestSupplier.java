package ru.vetoshkin.stress;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;

import java.util.function.Supplier;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class RequestSupplier implements Supplier<Request> {
    private static final Request postRequest = new RequestBuilder(HttpConstants.Methods.POST)
            .setUrl("http://localhost:8181/product/list/4/2")
            .build();
    private final Context context;
    private final int queryCount;


    public RequestSupplier(Context context, int i) {
        this.context = context;
        this.queryCount = i;
    }


    @Override
    public Request get() {
        // TODO:
        return postRequest;
    }

}
