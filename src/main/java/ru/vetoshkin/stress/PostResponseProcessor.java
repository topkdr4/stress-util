package ru.vetoshkin.stress;
import lombok.extern.slf4j.Slf4j;
import ru.vetoshkin.stress.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Slf4j
public class PostResponseProcessor implements Runnable {
    private final int batchSize;
    private final BlockingQueue<Response> dataSource;
    private final Storage storage;
    private final List<Consumer<Response>> pipeline = new ArrayList<>();
    private final AtomicInteger processed = new AtomicInteger();


    public PostResponseProcessor(Context context, int batchSize) {
        this.dataSource = context.getCompleteQueue();
        this.storage    = context.getStorage();
        this.batchSize  = batchSize;
    }


    public void addLast(Consumer<Response> consumer) {
        this.pipeline.add(consumer);
    }


    public int getProcessed() {
        return processed.get();
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

                processed.updateAndGet(operand -> operand + list.size());
            }
        } catch (Exception e) {
            log.error("Handle response error: {}", e);
            currentThread.interrupt();
        }
    }
}
