package se.tink.backend.common.config;

import java.util.function.Function;

public class IdentityStringConverterFactory extends StringConverterFactory {
    public IdentityStringConverterFactory() {
        builder.add(Function.identity());
    }
}
