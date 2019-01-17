import org.asynchttpclient.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class StressRunner {


    public static void main(String[] args) throws Exception {
        Context context = new Context(10);

        /*RequestSupplier  supplier = new RequestSupplier(context, 1_000);
        ResponseConsumer consumer = new ResponseConsumer(context);
        StatConsumer stat = new StatConsumer(context);*/

        //context.start(1_000);


        Storage storage = new Storage("stress_" + System.currentTimeMillis() + ".db");

        RequestSupplier  supplier = new RequestSupplier(context, 1_000_000);

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

        AtomicInteger inWork = new AtomicInteger(50);

        /**
         * Закидываем 1кк запросов
         */
        for (int i = 0; i < 1_000_000; i++) {
            while (inWork.get() == 0) {

            }

            inWork.decrementAndGet();
            if (i % 1500 == 0)
            System.out.println("SEND REQUEST " + (i + 1));

            ListenableFuture<Response> future = context.getAsyncHttpClient()
                    .executeRequest(supplier.get(), new ResponseHandler(context, i + 1));

            int finalI = i;
            future.addListener(() -> {
                try {
                    completeQueue.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("ERROR " + (finalI + 1));
                } finally {
                    inWork.incrementAndGet();
                }
            }, null);
        }







        while (inWork.get() != 50) {

        }

        while (handleResponsCount.get() > 0) {
            Thread.sleep(3000);
            System.out.println("handleResponsCount: " + handleResponsCount.get());
        }


        context.getAsyncHttpClient().close();
        System.exit(0);
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
