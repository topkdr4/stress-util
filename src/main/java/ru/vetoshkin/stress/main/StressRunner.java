package ru.vetoshkin.stress.main;
import ru.vetoshkin.stress.Context;
import ru.vetoshkin.stress.StatConsumer;
import ru.vetoshkin.stress.StressConfig;
import ru.vetoshkin.stress.config.Configuration;
import ru.vetoshkin.stress.util.Argument;
import ru.vetoshkin.stress.util.ArgumentParser;
import ru.vetoshkin.stress.util.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class StressRunner {


    public static void main(String[] args) throws Exception {
        ArgumentParser arguments = new ArgumentParser();

        Argument<Integer> argCount   = arguments.addInteger("-c",  "-count");
        Argument<Integer> argTimeout = arguments.addInteger("-to", "-timeout");
        Argument<Integer> argThreads = arguments.addInteger("-t",  "-threads");
        Argument<Integer> argRetry   = arguments.addInteger("-retry");
        Argument<String>  argHandler = arguments.addString("-h", "-handler");



        arguments.parse(args);

        StressConfig config = StressConfig.builder()
                .threads(argThreads.get(1))
                .requestCount(argCount.get(-1))
                .retry(argRetry.get(3))
                .timeout(argTimeout.get(60_000))
                .batchSize(argThreads.get(1) * 1000)
                .build();

        config.printConfig();


        Context context = new Context(config);

        new StatConsumer(context);
        context.start();
    }

}
