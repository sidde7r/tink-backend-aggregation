package se.tink.libraries.payloadparser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class PayloadParser {
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

    private PayloadParser() {}

    @SuppressWarnings("unchecked")
    public static <T> T parse(final String payload, final Class<T> clazz) {
        if (clazz.isMemberClass()) {
            throw new PayloadParserException(
                    "Could not instantiate member class. Choose non-member class.");
        }

        final Object[] split = SPACE_PATTERN.split(payload);
        Constructor<?> c = findMatchingConstructor(clazz, split.length);
        try {
            return (T) c.newInstance(split);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PayloadParserException(e);
        }
    }

    private static Constructor<?> findMatchingConstructor(final Class clazz, final int noOfArgs) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(
                        constructor -> {
                            constructor.setAccessible(true);
                            Class<?>[] params = constructor.getParameterTypes();
                            return params.length == noOfArgs
                                    && Arrays.stream(params)
                                            .allMatch(aClass -> aClass.equals(String.class));
                        })
                .findFirst()
                .orElseThrow(() -> new PayloadParserException("No matching constructor found."));
    }
}
