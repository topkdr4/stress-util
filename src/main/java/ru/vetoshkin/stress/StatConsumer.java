package ru.vetoshkin.stress;
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
        while (true) {
            System.out.println("RESP_PROCESSED: " + context.getResponseProcessedCount());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
