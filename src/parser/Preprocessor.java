package parser;

public class Preprocessor {
    public static String process(String input) {
        return input.replaceAll("[ \t]", "");
    }
}
