package ru.vetoshkin.stress;
import lombok.Builder;
import lombok.Getter;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import ru.vetoshkin.stress.config.Configuration;





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

    /**
     * Обработчик
     */
    private final ResponseProcessor processor;


    public static StressConfig build(Configuration conf) {


        return null;
    }


    @Builder(builderMethodName = "builder")
    public static StressConfig newConfig(
            int threads,
            int timeout,
            int retry,
            int requestCount,
            int batchSize,
            String processor
    ) {
        return new StressConfig(threads, timeout, retry, requestCount, batchSize, processor);
    }



    private StressConfig(
            int threads,
            int timeout,
            int retry,
            int requestCount,
            int batchSize,
            String processor
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

        this.processor = null;
    }


    public void printConfig() {
        System.out.println(""
                + "#     ____ ______ ___   ____ ____ ____     ______ ____   ____   __ \n"
                + "#    / __//_  __// _ \\ / __// __// __/____/_  __// __ \\ / __ \\ / / \n"
                + "#   _\\ \\   / /  / , _// _/ _\\ \\ _\\ \\ /___/ / /  / /_/ // /_/ // /__\n"
                + "#  /___/  /_/  /_/|_|/___//___//___/      /_/   \\____/ \\____//____/\n"
                + "#                                                                  "
                + "\n\n");

        System.out.println("Configuration:");
        // TODO: show config
    }
}
