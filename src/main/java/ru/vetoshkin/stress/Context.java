package ru.vetoshkin.stress;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;
import ru.vetoshkin.stress.storage.Storage;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;






@Slf4j
public class Context {
    private final AsyncHttpClient asyncHttpClient;

    @Getter private final AtomicBoolean active = new AtomicBoolean();

    /**
     * Фабрика запросов
     */
    private final RequestSupplier supplier;

    /**
     * Очередь ответов
     */
    @Getter private final BlockingQueue<Response> completeQueue = new LinkedBlockingQueue<>();

    /**
     * Хранилище результатов
     */
    @Getter private final Storage storage;

    private final Executor completePool = Executors.newSingleThreadExecutor();

    private final StressConfig config;

    @Getter private final PostResponseProcessor responseProcessor;


    public Context(StressConfig config) throws Exception {
        this.config = config;
        this.asyncHttpClient = Dsl.asyncHttpClient(config.getHttpClientConfig());
        this.storage = new Storage();
        this.responseProcessor = new PostResponseProcessor(this, config.getBatchSize());
        this.supplier = new RequestSupplier(this, config.getRequestCount());

        // TODO Обработка в груви
            /* responseProcessor.addLast(resp -> {

            });*/

        completePool.execute(responseProcessor);
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

        asyncHttpClient.close();
        active.set(false);
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
                log.error("Response error: {}", e);
                Thread.currentThread().interrupt();
            }
        }

    }

}
