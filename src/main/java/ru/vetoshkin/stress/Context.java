package ru.vetoshkin.stress;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import ru.vetoshkin.stress.storage.Storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;






@Slf4j
public class Context implements Closeable {
    /**
     * HTTP client
     */
    private final AsyncHttpClient asyncHttpClient;

    /**
     * Флаг активности
     */
    @Getter
    private final AtomicBoolean active = new AtomicBoolean();

    /**
     * Фабрика запросов
     */
    private final Producer supplier;

    /**
     * Очередь ответов
     */
    @Getter
    private final BlockingQueue<Response> completeQueue = new LinkedBlockingQueue<>();

    /**
     * Очередь запросов
     */
    private final BlockingQueue<Request> requestQueue;

    /**
     * Хранилище результатов
     */
    @Getter
    private final Storage storage;

    /**
     * Конфигурация
     */
    private final StressConfig config;

    /**
     * Обработчик ответов
     */
    @Getter
    private final PostResponseProcessor responseProcessor;


    public Context(StressConfig config) throws Exception {
        this.config = config;
        this.asyncHttpClient = Dsl.asyncHttpClient(config.getHttpClientConfig());
        this.storage = new Storage();

        this.requestQueue = new LinkedBlockingQueue<>((int)(config.getThreads() * 1.2));


        this.responseProcessor = new PostResponseProcessor(
                config.getBatchSize(),
                this.completeQueue,
                this.storage,
                null
                );
        this.supplier = new Producer(config.getRequestCount());

        Executors.newSingleThreadExecutor().execute(responseProcessor);

    }


    public void start() throws Exception {
        active.set(true);

        for (int i = 0; i < config.getThreads(); i++) {
            executeQuery();
        }


        // Ждем пока все не обработаем
        do {
            Thread.sleep(100);
        } while (responseProcessor.getProcessed() != config.getRequestCount());

        close();
    }


    private void executeQuery() {
        Request request = supplier.get();
        if (request == null)
            return;

        ListenableFuture<Response> future = asyncHttpClient.executeRequest(request, new ResponseHandler(this));
        future.addListener(new ResponseListener(future, this), null);
    }



    public void onError(Response response) {
        completeQueue.add(response);
    }


    @Override
    public void close() throws IOException {
        asyncHttpClient.close();
        active.set(false);
    }



    private static class ResponseListener implements Runnable {
        private final ListenableFuture<Response> future;
        private final Context context;


        private ResponseListener(ListenableFuture<Response> future, Context context) {
            this.future  = future;
            this.context = context;
        }


        @Override
        public void run() {
            try {
                context.completeQueue.add(future.get());
                context.executeQuery();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                context.executeQuery();
                // ignore
            }
        }

    }

}
