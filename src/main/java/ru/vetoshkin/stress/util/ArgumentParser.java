package ru.vetoshkin.stress.util;
import java.util.HashMap;
import java.util.Map;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class ArgumentParser {
    private Map<String, Argument<Boolean>> booleanArgs = new HashMap<>();
    private Map<String, Argument<Integer>> integerArgs = new HashMap<>();
    private Map<String, Argument<String>>  stringsArgs = new HashMap<>();




    public Argument<Boolean> addBoolean(String... aliases) {
        Argument<Boolean> arg = new Argument<>();
        for (int i = 0; i < aliases.length; i++) {
            booleanArgs.put(aliases[i], arg);
        }
        return arg;
    }


    public Argument<Integer> addInteger(String... aliases) {
        Argument<Integer> arg = new Argument<>();
        for (int i = 0; i < aliases.length; i++) {
            integerArgs.put(aliases[i], arg);
        }
        return arg;
    }


    public Argument<String> addString(String... aliases) {
        Argument<String> arg = new Argument<>();
        for (int i = 0; i < aliases.length; i++) {
            stringsArgs.put(aliases[i], arg);
        }
        return arg;
    }


    public void parse(String[] args) {
        for (String arg : args) {
            String[] params = arg.split("=");
            if (params.length != 2)
                throw new IllegalArgumentException("Illegal argument `" + arg + "`");

            String key   = params[0];
            String value = params[1];

            Argument<Boolean> booleanArgument = booleanArgs.get(key);
            Argument<Integer> integerArgument = integerArgs.get(key);
            Argument<String>  stringArgument  = stringsArgs.get(key);

            parseBoolean(booleanArgument, value);
            parseInteger(integerArgument, value);
            parseString(stringArgument, value);
        }
    }


    private static boolean checkArgument(Argument argument) {
        if (argument == null)
            return true;

        return argument.get() != null;

    }


    private void parseBoolean(Argument<Boolean> argument, String value) {
        if (checkArgument(argument))
            return;

        argument.get(Boolean.parseBoolean(value));
    }


    private void parseInteger(Argument<Integer> argument, String value) {
        if (checkArgument(argument))
            return;

        argument.set(Integer.parseInt(value));
    }


    private void parseString(Argument<String> argument, String value) {
        if (checkArgument(argument))
            return;

        argument.set(value);
    }

}
