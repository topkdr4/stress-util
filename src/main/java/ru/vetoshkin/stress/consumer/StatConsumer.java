package ru.vetoshkin.stress.consumer;
import ru.vetoshkin.stress.config.Configuration;
import ru.vetoshkin.stress.context.Context;
import ru.vetoshkin.stress.processor.PostResponseProcessor;
import ru.vetoshkin.stress.storage.Storage;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;





public class StatConsumer {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final String DELIMITER = "------------------------------------------------------------------------------------------------------------------";

    private final Storage storage;
    private final Context context;
    private final PostResponseProcessor processor;


    public StatConsumer(ru.vetoshkin.stress.context.Context context) {
        this.storage   = context.getStorage();
        this.context   = context;
        this.processor = context.getResponseProcessor();

        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);

        if (Configuration.Default.INFINITY_REQUEST == context.getRequestCount()) {
            scheduled.scheduleAtFixedRate(this::printInfinityStat, 0, 10, TimeUnit.SECONDS);
        } else {
            scheduled.scheduleAtFixedRate(this::printStat, 0, 10, TimeUnit.SECONDS);
        }

    }


    private void printInfinityStat() {
        try {
            StatData statData = storage.getStat();

            System.out.println(DELIMITER);
            System.out.printf("%20s | %15s | %15s |  %15s |  %15s |  %15s\n", "DATE_TIME", "ALL_REQUEST", "SUCCESS", "P_80", "P_90", "P_99");
            System.out.println(DELIMITER);
            System.out.printf("%20s | %15s | %15s | %15ss | %15ss | %15ss\n", DATE_FORMAT.format(new Date()), String.valueOf(statData.count), String.valueOf(statData.successCount), nanoToSec(statData.p80), nanoToSec(statData.p90), nanoToSec(statData.p99));
            System.out.println(DELIMITER);
            System.out.println("\n");
        } catch (Exception e) {
            // ignore
        }
    }


    private void printStat() {
        System.out.printf("COMPLETE: %.2f%%\n", (((processor.getProcessed() * 1.0) / context.getRequestCount())));
    }



    private String nanoToSec(long value) {
        return String.valueOf((double) value / 1_000_000_000.0);
    }


    public static class StatData {
        public long count;
        public long successCount;
        public long p80;
        public long p90;
        public long p99;
    }

}
