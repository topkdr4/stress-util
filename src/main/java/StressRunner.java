
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class StressRunner {


    public static void main(String[] args) throws Exception {
        ArgumentParser arguments = new ArgumentParser();

        Argument<Integer> argCount   = arguments.addInteger("-c" , "-count");
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

        Storage storage = new Storage("stress_" + System.currentTimeMillis() + ".db");


        final Queue<Response> completeQueue = new ConcurrentLinkedQueue<>();


        AtomicInteger handleResponsCount = new AtomicInteger(1_000_000);

        int batchSize = 50 * 3 * 3;



        Executor complete = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 3; i++) {
            complete.execute(() -> {
                try {
                    while (true) {
                        List<Response> list = fetchWhileFound(completeQueue, batchSize);
                        if (list.size() == 0)
                            continue;

                        for (int j = 0; j < list.size(); j++) {
                            handleResponsCount.decrementAndGet();
                        }

                        storage.insertResponses(list);
                        //System.out.println("RESPONSE " + TimeUnit.NANOSECONDS.toMillis(response.getEnd() - response.getStart()));
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            });
        }








/*
        while (inWork.get() != 50) {

        }

        while (handleResponsCount.get() > 0) {
            Thread.sleep(3000);
            System.out.println("handleResponsCount: " + handleResponsCount.get());
        }


        context.getAsyncHttpClient().close();
        System.exit(0);*/
    }


    private static <T> List<T> fetchWhileFound(Queue<T> queue, int max) {
        List<T> result = new ArrayList<>(max);
        for (int i = 0; i < max; i++) {
            T t = queue.poll();
            if (t == null)
                break;

            result.add(t);
        }

        return result;
    }
}
