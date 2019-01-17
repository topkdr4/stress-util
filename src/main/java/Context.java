import lombok.Getter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;

import javax.xml.ws.Response;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;





@Getter
public class Context {
    private final int threads;
    private final AsyncHttpClient asyncHttpClient;
    private final BlockingQueue<Request>  requestQueue       = new LinkedBlockingQueue<>(100);
    private final BlockingQueue<Response> responseQueue      = new LinkedBlockingQueue<>();
    private final BlockingQueue<Future<Response>> futureList = new LinkedBlockingQueue<>();
    private final CountDownLatch latch;
    private final AtomicInteger requestSendedCount = new AtomicInteger();
    private final AtomicInteger responseHandeledCount = new AtomicInteger();
    private final AtomicBoolean active = new AtomicBoolean();
    private final ResponseConsumer responseConsumer = new ResponseConsumer(this);
    private final RequestSupplier requestSupplier = new RequestSupplier(this, 1_000);





    public Context(int threads) {
        this.threads = threads;
        this.latch = new CountDownLatch(threads);
        this.asyncHttpClient = Dsl.asyncHttpClient(Config.HTTP_CLIENT_CONFIG);
    }


    public static void main(String[] args) throws Exception {
        new Context(10).start(1_000);
    }


    public void start(int requestCount) throws InterruptedException, IOException {
        this.requestSendedCount.set(requestCount);
        this.responseHandeledCount.set(requestCount);

        Context context = this;




        final Executor finishExecutor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < requestCount; i++) {
            Request request = context.getRequestQueue().take();
            ListenableFuture<Response> future = getAsyncHttpClient().executeRequest(request, new ResponseHandler(this));
            /*future.addListener(() -> {
                try {
                    Response response = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }, null);*/
        }



        while (requestSendedCount.get() != 0) {

        }

        while (responseHandeledCount.get() != 0) {

        }

        Thread.sleep(1000);

        asyncHttpClient.close();
        System.out.println("END");
        System.exit(0);
    }


    public void onError(Response response) {
        try {
            responseQueue.put(response);
        } catch (InterruptedException ignored) {
            System.out.println(ignored.getMessage());
            responseHandeledCount.decrementAndGet();
        }
    }



    public boolean hasRemainedRequest() {
        return requestSendedCount.decrementAndGet() >= 0;
    }


    public boolean hasRemainedResponse() {
        return responseHandeledCount.decrementAndGet() >= 0;
    }


    public boolean isActive() {
        return active.get();
    }
}
