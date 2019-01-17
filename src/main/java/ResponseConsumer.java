import java.util.function.Consumer;





/**
 * Ветошкин А.В. РИС-16бзу
 * */

public class ResponseConsumer implements Consumer<Response>, Runnable {
    private final Context context;


    public ResponseConsumer(Context context) {
        this.context = context;
    }


    @Override
    public void accept(Response response) {
        int val = context.getResponseHandeledCount().decrementAndGet();
        if (val % 35000 == 0)
            System.out.println(response);
    }


    @Override
    public void run() {
        try {
            while (context.getResponseHandeledCount().get() != 0) {
            //while (context.hasRemainedResponse()) {
                this.accept(context.getResponseQueue().take());
               // this.accept(context.getPriorityQueue().take().get());
            }
            System.out.println("CONSUMER " + Thread.currentThread().getName() + " END");
        } catch (Exception ignored) {

        }
    }
}
