import lombok.Getter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;





@Getter
public class Context {
    private final int threads;
    private final AsyncHttpClient asyncHttpClient;
    private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>(100);
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Future<Response>> futureList = new LinkedBlockingQueue<>();
    private final CountDownLatch latch;
    private final AtomicInteger requestSendedCount = new AtomicInteger();
    private final AtomicInteger responseHandeledCount = new AtomicInteger();

    private final Executor compilationExecutor = Executors.newFixedThreadPool(3);
    private final CompletionService<CompletableFuture<Response>> completionService = new ExecutorCompletionService<CompletableFuture<Response>>(compilationExecutor, futureList);


    public Context(int threads) {
        this.threads = threads;
        this.latch = new CountDownLatch(threads);
        this.asyncHttpClient = Dsl.asyncHttpClient(Config.HTTP_CLIENT_CONFIG);
    }


    public static void main(String[] args) throws Exception {
        new Context(50).start(1_000_000);
    }


    public void start(int requestCount) throws InterruptedException, IOException {
        this.requestSendedCount.set(requestCount);
        this.responseHandeledCount.set(requestCount);

        /*RequestSupplier supplier = new RequestSupplier(this, requestCount);
        Thread requestGenerator = new Thread(supplier);
        requestGenerator.setDaemon(true);
        requestGenerator.start();


        for (int i = 0; i < 3; i++) {
            ResponseConsumer consumer = new ResponseConsumer(this);
            Thread postResponse = new Thread(consumer);
            postResponse.setName("RESP_CONSUMER-" + (i + 1));
            postResponse.setDaemon(true);
            postResponse.start();
        }*/


        Thread stat = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    System.out.println("SEND_ELAPSED: " + requestSendedCount.get() + " RESP_ELAPSED: " + responseHandeledCount.get() + " REQT_QUEUE: " + requestQueue.size() + " RESP_QUEUE: " + responseQueue.size());
                    if (requestSendedCount.get() <= 0 && responseHandeledCount.get() <= 0)
                        break;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        stat.setDaemon(true);
        stat.start();


/*
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(new RequestSender(this));
            thread.setDaemon(true);
            thread.start();
        }
*/

        Context context = this;

        for (int i = 0; i < requestCount; i++) {
            Request request = context.getRequestQueue().take();
            getAsyncHttpClient().executeRequest(request).toCompletableFuture();

        }


        final Executor finishExecutor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            finishExecutor.execute(() -> {
                try {
                    ResponseConsumer consumer = new ResponseConsumer(context);
                    while (context.getRequestSendedCount().get() != 0 && context.hasRemainedResponse()) {
                        completionService.take();
                        try {
                            consumer.accept(future.get());
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }


        //latch.await();

        while (requestSendedCount.get() != 0) {

        }

        while (responseHandeledCount.get() != 0) {

        }

        Thread.sleep(1000);

        asyncHttpClient.close();
        System.out.println("END");
        System.exit(0);
    }


    public void onError(CompletableFuture<Response> response) {
        completionService.submit(() -> {}, response);
        /*
        try {
            completionService.submit(() -> {}, response);
            //responseQueue.put(response);
        } catch (InterruptedException ignored) {
            System.out.println(ignored.getMessage());
            responseHandeledCount.decrementAndGet();
        }*/
    }



    public boolean hasRemainedRequest() {
        return requestSendedCount.decrementAndGet() >= 0;
    }


    public boolean hasRemainedResponse() {
        return responseHandeledCount.decrementAndGet() >= 0;
    }
}
