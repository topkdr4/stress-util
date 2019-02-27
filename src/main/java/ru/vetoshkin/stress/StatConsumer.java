package ru.vetoshkin.stress;
import ru.vetoshkin.stress.storage.Storage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;





public class StatConsumer {
    private final Storage storage;


    public StatConsumer(ru.vetoshkin.stress.context.Context context) {
        this.storage = context.getStorage();
        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        scheduled.scheduleAtFixedRate(this::accept, 0, 3, TimeUnit.SECONDS);
        scheduled.execute(this::accept);
    }


    private void accept() {
        System.out.println("------------------");
       // System.out.println("RESP_PROCESSED:  " + context.getResponseProcessor().getProcessed());
        //System.out.println("RESP_QUEUE_SIZE: " + context.getCompleteQueue().size());
    }

}
