package ru.vetoshkin.stress.processor;
import lombok.extern.slf4j.Slf4j;
import ru.vetoshkin.stress.Response;
import ru.vetoshkin.stress.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.vetoshkin.stress.config.Configuration.Default.GROOVY_HANDLER;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Slf4j
public class PostResponseProcessor implements Runnable {
    /**
     * Размер порции
     */
    private final int batchSize;

    /**
     * Очередь ответов
     */
    private final BlockingQueue<Response> dataSource;

    /**
     * Хранилище
     */
    private final Storage storage;

    /**
     * Обработчик ответов
     */
    private final ResponseProcessor processor;

    /**
     * Количество обработанных ответов
     */
    private final AtomicInteger processed = new AtomicInteger();


    public PostResponseProcessor(
            BlockingQueue<Response> dataSource,
            Storage storage,
            ResponseProcessor processor,
            int batchSize
            ) {
        this.dataSource = dataSource;
        this.storage    = storage;
        this.processor  = processor != null ? processor : GROOVY_HANDLER;
        this.batchSize  = batchSize;
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


                storage.insertResponses(list);

                processed.updateAndGet(operand -> operand + list.size());
            }
        } catch (Exception e) {
            log.error("Handle response error: {}", e);
            currentThread.interrupt();
        }
    }
}
