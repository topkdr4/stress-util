import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class ResponseConsumer implements Consumer<Response> {
    private final Executor executor = Executors.newFixedThreadPool(3);
    private final Context context;


    public ResponseConsumer(Context context) {
        this.context = context;

        for (int i = 0; i < 3; i++) {
            executor.execute(this::fetch);
        }
    }



    public void fetch() {
        try {
            while (context.hasRemainedResponse()) {
                Response response = context.getResponseQueue().take();
                this.accept(response);
            }
        } catch (Exception ignored) {

        }
    }


    @Override
    public void accept(Response response) {
        System.out.println(response);
    }
}
