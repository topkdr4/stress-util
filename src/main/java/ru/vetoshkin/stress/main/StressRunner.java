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


        StressConfig config = new StressConfig.Builder()
                .setThreads(argThreads.get(1))
                .setRequestCount(argCount.get(-1))
                .setRetry(argRetry.get(3))
                .setTimeout(argTimeout.get(60_000))
                .build();



        Context context = new Context(config);
        new StatConsumer(context);
        context.start();
    }

}
