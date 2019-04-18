package ru.vetoshkin.example

import ru.vetoshkin.stress.Response
import ru.vetoshkin.stress.processor.ResponseProcessor

import java.util.concurrent.ThreadLocalRandom

/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class Handler implements ResponseProcessor {

    @Override
    public boolean process(Response response) {
        return ThreadLocalRandom.current().nextBoolean();
    }

}
