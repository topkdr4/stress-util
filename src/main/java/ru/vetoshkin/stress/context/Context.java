package ru.vetoshkin.stress.context;
import lombok.Getter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import ru.vetoshkin.stress.*;
import ru.vetoshkin.stress.config.Role;
import ru.vetoshkin.stress.storage.Storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public abstract class Context implements Closeable {
    /**
     * Флаг активности
     */
    @Getter
    protected final AtomicBoolean active = new AtomicBoolean();

    /**
     * Очередь ответов
     */
    @Getter
    private final BlockingQueue<Response> completeQueue = new LinkedBlockingQueue<>();

    /**
     * Хранилище результатов
     */
    @Getter
    private final Storage storage = new Storage();

    /**
     * HTTP client
     */
    private final AsyncHttpClient asyncHttpClient;

    /**
     * Фабрика запросов
     */
    private final Producer supplier;

    /**
     * Очередь запросов
     */
    private final BlockingQueue<Request> requestQueue;

    /**
     * Обработчик ответов
     */
    @Getter
    protected final PostResponseProcessor responseProcessor;

    /**
     * Количество запросов
     */
    protected final int requestCount;

    /**
     * Количество потоков
     */
    protected final int threads;




    Context(StressConfig config) {
        this.requestCount = config.getRequestCount();
        this.threads = config.getThreads();
        this.asyncHttpClient = Dsl.asyncHttpClient(config.getHttpClientConfig());

        this.requestQueue = new LinkedBlockingQueue<>((int)(config.getThreads() * 1.2));


        this.responseProcessor = new PostResponseProcessor(
                config.getBatchSize(),
                this.completeQueue,
                this.storage,
                config.getGroovyHandler()
        );
        this.supplier = new Producer(config.getRequestCount());

        Executors.newSingleThreadExecutor().execute(responseProcessor);
    }


    public abstract void start() throws Exception;


    public void onError(Response response) {
        completeQueue.add(response);
    }


    protected void executeQuery() {
        Request request = supplier.get();
        if (request == null)
            return;

        ListenableFuture<Response> future = asyncHttpClient.executeRequest(request, new ResponseHandler(this));
        future.addListener(new ResponseListener(future, this), null);
    }


    @Override
    public void close() throws IOException {
        asyncHttpClient.close();
        active.set(false);
    }



    public static Context create(StressConfig config) throws Exception {
        Role role = config.getRole();
        if (role == null)
            throw new IllegalArgumentException("param `role` not defined");

        switch (role) {
            case SLAVE:
                return new SlaveContext(config);

            case LEADER:
                return new LeaderContext(config);

            case SINGLE:
                return new SingleContext(config);
        }

        throw new IllegalArgumentException("param `role` unknown");
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
                // java.util.concurrent.TimeoutException
                System.err.println(e.getMessage());
                context.executeQuery();
                // ignore
            }
        }

    }
}
