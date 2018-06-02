package se.tink.backend.common.config;

public class LowerCaseStringConverterFactory extends StringConverterFactory {
    LowerCaseStringConverterFactory() {
        builder.add((String s) -> s.toLowerCase());
    }
}
