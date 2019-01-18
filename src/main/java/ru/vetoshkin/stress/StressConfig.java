package ru.vetoshkin.stress;
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



    private StressConfig(Builder builder) {
        this.httpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setIoThreadsCount(builder.threads)
                .setMaxConnections(builder.threads)
                .setConnectTimeout(builder.timeout)
                .setMaxRequestRetry(builder.retry)
                .build();


        this.requestCount = builder.requestCount == 0 ? -1 : builder.requestCount;

        this.batchSize = builder.batchSize;

        this.threads = builder.threads;
    }



    public static class Builder {
        private int threads;
        private int timeout;
        private int retry;
        private String groovyScriptPath;
        private int requestCount;
        private int batchSize;


        public Builder setThreads(int threads) {
            this.threads   = threads;
            this.batchSize = threads * 1000;
            return this;
        }


        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }


        public Builder setRetry(int retry) {
            this.retry = retry;
            return this;
        }


        public Builder setGroovyScriptPath(String groovyScriptPath) {
            this.groovyScriptPath = groovyScriptPath;
            return this;
        }


        public Builder setRequestCount(int requestCount) {
            this.requestCount = requestCount;
            return this;
        }


        public StressConfig build() {
            return new StressConfig(this);
        }
    }

}
