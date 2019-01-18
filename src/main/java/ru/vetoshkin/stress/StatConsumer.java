package ru.vetoshkin.stress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;





public class StatConsumer {
    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
    private final Context context;


    public StatConsumer(Context context) {
        this.context = context;
        this.scheduled.scheduleAtFixedRate(this::accept, 0, 3, TimeUnit.SECONDS);
        this.scheduled.execute(this::accept);
    }


    private void accept() {
        System.out.println("------------------");
        System.out.println("RESP_PROCESSED:  " + context.getResponseProcessedCount());
        System.out.println("RESP_QUEUE_SIZE: " + context.getCompleteQueue().size());
    }

}
