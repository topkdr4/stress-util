package ru.vetoshkin.stress.context;
import ru.vetoshkin.stress.StressConfig;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
class SingleContext extends Context {
    SingleContext(StressConfig config) throws Exception {
        super(config);
    }


    @Override
    public void start() throws Exception {
        active.set(true);

        for (int i = 0; i < threads; i++) {
            executeQuery();
        }


        // Ждем пока все не обработаем
        do {
            Thread.sleep(100);
        } while (responseProcessor.getProcessed() != requestCount);

        close();
    }
}
