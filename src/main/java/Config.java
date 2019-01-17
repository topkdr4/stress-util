import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;





public class Config {

    /**
     * Конфигурация Http клиента
     */
    public static final AsyncHttpClientConfig HTTP_CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setIoThreadsCount(50)
            .setMaxConnections(50)
            .setConnectTimeout(60_000)
            .setMaxRequestRetry(3)
            .build();



    /**
     * Размер пула на отправку запросов
     */
    public int THREAD_POOL_REQUEST_SIZE = 1;





}
