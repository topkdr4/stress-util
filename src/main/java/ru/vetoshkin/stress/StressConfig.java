package ru.vetoshkin.stress;
import lombok.Builder;
import lombok.Getter;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;





@Getter
public class StressConfig {

    /**
     * Конфигурация Http клиента
     */
    private final AsyncHttpClientConfig httpClientConfig;


    /**
     * Количество отправляемых запросов
     */
    private final int requestCount;


    /**
     * По сколько вычитывать ответов
     */
    private final int batchSize;


    /**
     * Колиечество потоков
     */
    private final int threads;


    @Builder(builderMethodName = "builder")
    public static StressConfig newConfig(
            int threads,
            int timeout,
            int retry,
            int requestCount,
            int batchSize
    ) {
        return new StressConfig(threads, timeout, retry, requestCount, batchSize);
    }



    private StressConfig(
            int threads,
            int timeout,
            int retry,
            int requestCount,
            int batchSize
    ) {
        this.httpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setIoThreadsCount(threads)
                .setMaxConnections(threads)
                .setConnectTimeout(timeout)
                .setMaxRequestRetry(retry)
                .build();


        this.requestCount = requestCount == 0 ? -1 : requestCount;

        this.batchSize = batchSize;

        this.threads = threads;
    }
}
