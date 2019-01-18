package ru.vetoshkin.stress.main;
import ru.vetoshkin.stress.Context;
import ru.vetoshkin.stress.StatConsumer;
import ru.vetoshkin.stress.StressConfig;
import ru.vetoshkin.stress.util.Argument;
import ru.vetoshkin.stress.util.ArgumentParser;





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


        arguments.parse(args);


        StressConfig config = StressConfig.builder()
                .threads(argThreads.get(1))
                .requestCount(argCount.get(-1))
                .retry(argRetry.get(3))
                .timeout(argTimeout.get(60_000))
                .batchSize(argThreads.get(1) * 1000)
                .build();



        Context context = new Context(config);
        new StatConsumer(context);
        context.start();
    }

}
