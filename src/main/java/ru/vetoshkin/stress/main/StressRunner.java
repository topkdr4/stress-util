package ru.vetoshkin.stress.main;
import lombok.extern.slf4j.Slf4j;
import ru.vetoshkin.stress.consumer.StatConsumer;
import ru.vetoshkin.stress.StressConfig;
import ru.vetoshkin.stress.config.Configuration;
import ru.vetoshkin.stress.context.Context;
import ru.vetoshkin.stress.util.Json;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Slf4j
public class StressRunner {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);


    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("first arg is path to configuration file");
            return;
        }

        StressConfig stressConfig;

        try {
            Path configPath = Paths.get(args[0]);
            byte[] bytes = Files.readAllBytes(configPath);

            String configSource = new String(bytes, StandardCharsets.UTF_8);
            Configuration configuration = Json.getJsonMapper().readValue(configSource, Configuration.class);
            stressConfig = StressConfig.build(configuration);
        } catch (Exception e) {
            log.error("configuration error {}", e);
            return;
        }

        stressConfig.printConfig();

        Context context = Context.create(stressConfig);

        long beforeStart = 0;
        Date startDate = stressConfig.getStartDate();

        if (startDate != null) {
            beforeStart = startDate.getTime() - System.currentTimeMillis();
        }

        StatConsumer consumer = new StatConsumer(context);

        SCHEDULER.schedule(() -> {
            consumer.start();
            context.start();
        }, beforeStart, TimeUnit.MILLISECONDS);


        Date endDate = stressConfig.getEndDate();

        if (endDate != null) {
            long beforeEnd = endDate.getTime() - System.currentTimeMillis();

            SCHEDULER.schedule(() -> {
                consumer.printResult();
                consumer.close();
                context.close();
            }, beforeEnd, TimeUnit.MILLISECONDS);
        }
    }

}
