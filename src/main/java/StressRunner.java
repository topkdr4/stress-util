import org.asynchttpclient.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

        RequestSupplier  supplier = new RequestSupplier(context, 1_000_000);
        Executor complete = Executors.newFixedThreadPool(3);
        AtomicInteger inWork = new AtomicInteger(50);

        /**
         * Закидываем 1кк запросов
         */
        for (int i = 0; i < 1_000_000; i++) {
            while (inWork.get() == 0) {

            }

            inWork.decrementAndGet();
            System.out.println("SEND REQUEST " + (i + 1));

            ListenableFuture<Response> future = context.getAsyncHttpClient()
                    .executeRequest(supplier.get(), new ResponseHandler(context, i + 1));

            int finalI = i;
            future.addListener(() -> {
                try {
                    Response response = future.get();
                    System.out.println("RESPONSE " + (finalI + 1) + " " + TimeUnit.NANOSECONDS.toMillis(response.getEnd() - response.getStart()));
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("ERROR " + (finalI + 1));
                } finally {
                    inWork.incrementAndGet();
                }
            }, complete);
        }




        while (inWork.get() != 50) {

        }


        context.getAsyncHttpClient().close();
        System.exit(0);
    }

}
