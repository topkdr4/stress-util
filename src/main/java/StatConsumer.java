import java.util.concurrent.Executor;
import java.util.concurrent.Executors;





public class StatConsumer {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;


    public StatConsumer(Context context) {
        this.context = context;
        this.executor.execute(this::accept);
    }


    private void accept() {
        while (!Thread.currentThread().isInterrupted() && context.isActive()) {

        }
    }

}
