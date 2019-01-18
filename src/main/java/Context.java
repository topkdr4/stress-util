import lombok.Getter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;





@Getter
public class Context {
    private final AsyncHttpClient asyncHttpClient;
    private final AtomicInteger requestSendedCount = new AtomicInteger();
    private final AtomicInteger responseProcessedCount = new AtomicInteger();
    private final AtomicBoolean active = new AtomicBoolean();
    private final ResponseConsumer responseConsumer = new ResponseConsumer(this);
    private final RequestSupplier requestSupplier = new RequestSupplier(this, 1_000);
    /**
     * Фабрика запросв
     */
    private final Supplier<Request> supplier = new RequestSupplier(this, 0);
    /**
     * Очередь ответов
     */
    private final BlockingQueue<Response> completeQueue = new LinkedBlockingQueue<>();


    public Context(StressConfig config) {

        this.asyncHttpClient = Dsl.asyncHttpClient(config.getHttpClientConfig());
    }


    public void start() {
        AtomicInteger inWork = new AtomicInteger(50);

        for (int i = 0; i < 1_000_000; i++) {
            while (inWork.get() == 0) {

            }

            inWork.decrementAndGet();
            ListenableFuture<Response> future = getAsyncHttpClient()
                    .executeRequest(supplier.get(), new ResponseHandler(this, i + 1));

            int finalI = i;
            future.addListener(() -> {
                try {
                    completeQueue.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("ERROR " + (finalI + 1));
                } finally {
                    inWork.incrementAndGet();
                }
            }, null);
        }
    }



    public void onError(Response response) {
        completeQueue.add(response);
    }



    public boolean hasRemainedRequest() {
        return requestSendedCount.decrementAndGet() >= 0;
    }


    public boolean hasRemainedResponse() {
        return responseProcessedCount.decrementAndGet() >= 0;
    }


    public boolean isActive() {
        return active.get();
    }
}
