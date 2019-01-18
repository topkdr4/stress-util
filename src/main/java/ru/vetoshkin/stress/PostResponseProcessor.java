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
    private final List<Consumer<Response>> pipeline = new ArrayList<>();
    private final AtomicInteger counter;


    public PostResponseProcessor(Context context, int batchSize) {
        this.dataSource = context.getCompleteQueue();
        this.storage    = context.getStorage();
        this.counter    = context.getResponseProcessedCount();
        this.batchSize  = batchSize;
    }


    public void addLast(Consumer<Response> consumer) {
        this.pipeline.add(consumer);
    }


    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        try {
            while (!currentThread.isInterrupted()) {
                List<Response> list = new ArrayList<>(batchSize);
                list.add(dataSource.take());
                dataSource.drainTo(list, batchSize);

                for (Response response : list) {
                    for (Consumer<Response> consumer : pipeline) {
                        consumer.accept(response);
                    }
                }

                storage.insertResponses(list);

                counter.updateAndGet(operand -> operand - list.size());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            currentThread.interrupt();
        }
    }
}
