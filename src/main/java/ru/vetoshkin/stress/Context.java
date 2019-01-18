package ru.vetoshkin.stress;
import lombok.Getter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import ru.vetoshkin.stress.storage.Storage;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;






public class Context {
    private final AsyncHttpClient asyncHttpClient;

    /**
     * Счетчик обработанных ответов
     */
    @Getter private final AtomicInteger responseProcessedCount = new AtomicInteger();
    @Getter private final AtomicBoolean active = new AtomicBoolean();

    /**
     * Фабрика запросов
     */
    private final Supplier<Request> supplier = new RequestSupplier(this, 0);

    /**
     * Очередь ответов
     */
    @Getter private final BlockingQueue<Response> completeQueue = new LinkedBlockingQueue<>();

    /**
     * Хранилище результатов
     */
    @Getter private final Storage storage;

    private final Executor completePool = Executors.newFixedThreadPool(3);

    private final StressConfig config;


    public Context(StressConfig config) throws Exception {
        this.config = config;
        this.asyncHttpClient = Dsl.asyncHttpClient(config.getHttpClientConfig());
        this.storage = new Storage("stress_" + System.currentTimeMillis() + ".db");

        for (int i = 0; i < 1; i++) {
            PostResponseProcessor processor = new PostResponseProcessor(this, config.getBatchSize());

            // TODO Обработка в груви
            /* processor.addLast(resp -> {

            });*/

            completePool.execute(processor);
        }
    }


    public void start() throws IOException {
        AtomicInteger inWork = new AtomicInteger(config.getThreads());

        active.set(true);


        int requestCount = config.getRequestCount();
        this.responseProcessedCount.set(requestCount);

        for (int i = 0; i < requestCount; i++) {
            while (inWork.get() == 0) {

            }

            inWork.decrementAndGet();
            ListenableFuture<Response> future = asyncHttpClient
                    .executeRequest(supplier.get(), new ResponseHandler(this));

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

        while (responseProcessedCount.get() > 0) {

        }

        asyncHttpClient.close();
        System.exit(0);
    }



    public void onError(Response response) {
        completeQueue.add(response);
    }

}
