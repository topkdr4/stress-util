package ru.vetoshkin.stress;
import lombok.extern.slf4j.Slf4j;
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
    private final int batchSize = 1000;

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
     * Список ответов
     */
    private final long[] data = new long[10_000_000];

    private long min;
    private long max;
    private int size;


    public PostResponseProcessor(
            BlockingQueue<Response> dataSource,
            Storage storage,
            ResponseProcessor processor
            ) {
        this.dataSource = dataSource;
        this.storage    = storage;
        this.processor  = processor != null ? processor : GROOVY_HANDLER;
    }


    public int getProcessed() {
        return processed.get();
    }


    public long quantile(double qnt) {
        if (size == 0)
            return 0;

        int index = (int) (qnt * size);

        if (index >= size)
            index = size - 1;

        return data[index];
    }


    public long percentile(int per) {
        return quantile(per / 100.0);
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
                    if (!response.isTransportError())
                        response.setSuccess(processor.process(response));

                    long diff = response.getDiffTime();
                    if (size == 0 || max < diff)
                        max = diff;

                    if (size == 0 || diff < min)
                        min = diff;

                    int last = size;

                    while (last > 0 && data[last - 1] > diff) last--;

                    if (size > last) {
                        System.arraycopy(data, last, data, last + 1, size - last);
                    }

                    data[last] = diff;
                    size++;
                }

                //storage.insertResponses(list);

                processed.updateAndGet(operand -> operand + list.size());
            }
        } catch (Exception e) {
            log.error("Handle response error: {}", e);
            currentThread.interrupt();
        }
    }
}
