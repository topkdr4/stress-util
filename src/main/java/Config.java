import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;





public class Config {

    /**
     * Конфигурация Http клиента
     */
    public static final AsyncHttpClientConfig HTTP_CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setIoThreadsCount(5)
            .setMaxConnections(5)
            .build();



    /**
     * Размер пула на отправку запросов
     */
    public int THREAD_POOL_REQUEST_SIZE = 1;





}
