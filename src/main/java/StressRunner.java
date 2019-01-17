/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class StressRunner {


    public static void main(String[] args) throws Exception {
        Context context = new Context(10);

        RequestSupplier  supplier = new RequestSupplier(context, 1_000);
        ResponseConsumer consumer = new ResponseConsumer(context);
        StatConsumer stat = new StatConsumer(context);

        context.start(1_000);
    }

}
