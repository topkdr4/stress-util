import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Request;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class RequestSender implements Runnable {
    private final Context context;


    public RequestSender(Context context) {
        this.context = context;
    }


    @Override
    public void run() {
        try {
            context.getLatch().countDown();
            context.getLatch().await();

            while (!Thread.interrupted() && context.hasRemainedRequest()) {
                Request request = context.getRequestQueue().take();

                ListenableFuture<Response> future = context.getAsyncHttpClient()
                        .executeRequest(request, new ResponseHandler(context));

                //Response response = future.toCompletableFuture().join();
                //context.getResponseQueue().put(response);
            }
            System.out.println("SENDER " + Thread.currentThread().getName() + " END");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
