package ru.vetoshkin.stress.context;
import lombok.Getter;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import ru.vetoshkin.stress.*;
import ru.vetoshkin.stress.config.Role;
import ru.vetoshkin.stress.processor.PostResponseProcessor;
import ru.vetoshkin.stress.producer.Producer;
import ru.vetoshkin.stress.storage.Storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
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
    private final Producer producer;

    /**
     * Обработчик ответов
     */
    @Getter
    protected final PostResponseProcessor responseProcessor;

    /**
     * Количество запросов
     */
    @Getter
    protected final int requestCount;

    /**
     * Количество потоков
     */
    protected final int threads;




    Context(StressConfig config) {
        this.requestCount = config.getRequestCount();
        this.threads = config.getThreads();
        this.asyncHttpClient = Dsl.asyncHttpClient(config.getHttpClientConfig());


        this.responseProcessor = new PostResponseProcessor(
                this.completeQueue,
                this.storage,
                config.getGroovyHandler(),
                config.getBatchSize()
        );


        this.producer = new Producer(config.getRequestCount());

        Executors.newSingleThreadExecutor().execute(responseProcessor);
    }


    public abstract void start() throws RuntimeException;


    public void onError(Response response) {
        completeQueue.add(response);
    }


    protected void executeQuery() {
        Request request = producer.get();
        if (request == null)
            return;

        ListenableFuture<Response> future = asyncHttpClient.executeRequest(request, new ResponseHandler(this));
        future.addListener(new ResponseListener(future, this), null);
    }


    @Override
    public void close() {
        active.set(false);
        try {
            while (!asyncHttpClient.isClosed())
                asyncHttpClient.close();

            responseProcessor.stop();
        } catch (Exception e) {
            e.printStackTrace();
            // ignore
        } finally {
            System.exit(0);
        }
    }



    public static Context create(StressConfig config) throws Exception {
        Role role = config.getRole();
        if (role == null)
            throw new IllegalArgumentException("param `role` not defined");

        switch (role) {
            case SLAVE:
            case LEADER:
                //return new SlaveContext(config);
                //return new LeaderContext(config);
                throw new UnsupportedOperationException("Role `" + role + "` is not supported");

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
                context.executeQuery();
            }
        }

    }
}
