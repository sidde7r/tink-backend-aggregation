package se.tink.backend.aggregation.utils;

import java.util.Collection;

public class ClientConfigurationStringMasker implements StringMasker {

    private final Collection<String> sensitiveValuesToMask;

    public ClientConfigurationStringMasker(Collection<String> sensitiveValuesToMask) {
        this.sensitiveValuesToMask = sensitiveValuesToMask;
    }

    @Override
    public String getMasked(String string) {
        return sensitiveValuesToMask.stream()
                .reduce(string, (s1, value) -> s1.replace(value, MASK));
    }
}
