package ru.vetoshkin.stress.processor;
import lombok.extern.slf4j.Slf4j;
import ru.vetoshkin.stress.Response;
import ru.vetoshkin.stress.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;






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

    /**
     * Флаг прерывания
     */
    private final AtomicBoolean stop = new AtomicBoolean(false);


    public PostResponseProcessor(
            BlockingQueue<Response> dataSource,
            Storage storage,
            ResponseProcessor processor,
            int batchSize
            ) {
        this.dataSource = dataSource;
        this.storage    = storage;
        this.processor  = processor;
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
                if (dataSource.size() == 0 && stop.get())
                    break;

                List<Response> list = new ArrayList<>(batchSize);
                list.add(dataSource.take());
                dataSource.drainTo(list, batchSize);

                for (Response resp : list) {
                    resp.setSuccess(processor.process(resp));
                }

                storage.insertResponses(list);

                processed.updateAndGet(operand -> operand + list.size());
            }
        } catch (Exception e) {
            log.error("Handle response error: {}", e);
            currentThread.interrupt();
        }
    }

    public void stop() {
        stop.set(true);
    }
}
