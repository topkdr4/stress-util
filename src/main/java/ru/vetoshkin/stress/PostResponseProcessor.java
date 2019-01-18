package ru.vetoshkin.stress;
import ru.vetoshkin.stress.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class PostResponseProcessor implements Runnable {
    private final int batchSize;
    private final BlockingQueue<Response> dataSource;
    private final Storage storage;
    private final List<Consumer<Response>> pipline = new ArrayList<>();
    private final AtomicInteger counter;


    public PostResponseProcessor(Context context, int batchSize) {
        this.dataSource = context.getCompleteQueue();
        this.storage    = context.getStorage();
        this.counter    = context.getResponseProcessedCount();
        this.batchSize  = batchSize;
    }


    public void addLast(Consumer<Response> consumer) {
        this.pipline.add(consumer);
    }


    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        try {
            while (!currentThread.isInterrupted()) {
                Response first = dataSource.take();
                List<Response> list = new ArrayList<>(batchSize);
                dataSource.drainTo(list, batchSize);

                list.add(first);

                for (Response response : list) {
                    for (Consumer<Response> consumer : pipline) {
                        consumer.accept(response);
                    }
                }

                storage.insertResponses(list);

                for (int j = 0; j < list.size(); j++) {
                    counter.decrementAndGet();
                }
            }
        } catch (Exception e) {
            currentThread.interrupt();
        }
    }
}
