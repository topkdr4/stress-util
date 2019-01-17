import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.util.HttpConstants;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class RequestSupplier implements Supplier<Request>, Runnable {
    private static final Request postRequest = new RequestBuilder(HttpConstants.Methods.POST)
            .setUrl("http://bikeshop.hopto.org:8080/product/list/4/2")
            .build();
    private final Context context;
    private final int queryCount;


    public RequestSupplier(Context context, int i) {
        this.context = context;
        this.queryCount = i;
    }


    @Override
    public Request get() {
        return postRequest;
    }


    @Override
    public void run() {
        for (int i = 0; i < queryCount; i++) {
            try {
                context.getRequestQueue().put(get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
