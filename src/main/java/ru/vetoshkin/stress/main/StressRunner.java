package ru.vetoshkin.stress.main;
import ru.vetoshkin.stress.consumer.StatConsumer;
import ru.vetoshkin.stress.StressConfig;
import ru.vetoshkin.stress.config.Configuration;
import ru.vetoshkin.stress.context.Context;
import ru.vetoshkin.stress.util.Json;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class StressRunner {


    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("first arg is path to configuration file");
            return;
        }

        StressConfig stressConfig;

        try {
            Path configPath = Paths.get(args[0]);
            byte[] bytes = Files.readAllBytes(configPath);

            String configSource = new String(bytes, StandardCharsets.UTF_8);
            Configuration configuration = Json.getJsonMapper().readValue(configSource, Configuration.class);
            stressConfig = StressConfig.build(configuration);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        stressConfig.printConfig();

        Context context = Context.create(stressConfig);

        new StatConsumer(context);
        context.start();
    }

}
