package se.tink.libraries.payloadparser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Currently there is no good way to provide a configuration specific to a financial institution
 * that uses a shared agent with other institutions (like service providers agents). One of the
 * options is to use provider's payload field with required data separated with defined separator.
 * <br>
 * The operation of this class is dividing the input value with space and creating a configuration
 * object with these values. The object is created using a constructor whose number of parameters is
 * equal to the number of values in the payload. The arguments of the constructor must be of String
 * type. The PayloadParser can set up objects with private constructor as well. Currently the only
 * supported configuration classes are non-member classes.<br>
 * Example: <code>
 * class ConfigData {
 *     private String param1;
 *     private String param2;
 *
 *     ConfigData(String param1, String param2) {
 *         this.param1 = param1;
 *         this.param2 = param2;
 *     }
 * }
 *
 * ConfigData configData = PayloadParser.parse("value1 value2", ConfigData.class);
 * </code>
 */
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

    /**
     * Finds constructor able to create object for a given number of input parameters
     *
     * @param clazz configuration class to check for valid constructor
     * @param noOfArgs number of parameters that are needed in constructor to be valid
     * @return proper constructor to create configuration object
     * @throws PayloadParserException if no constructor found
     */
    private static Constructor<?> findMatchingConstructor(final Class clazz, final int noOfArgs) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> isProperConstructor(constructor, noOfArgs))
                .findFirst()
                .orElseThrow(() -> new PayloadParserException("No matching constructor found."));
    }

    /**
     * This class verifies does constructor have proper number of input parameters and all of them
     * are type of String.
     *
     * @return true if constructor can create required configuration object, false otherwise
     */
    private static boolean isProperConstructor(final Constructor constructor, final int noOfArgs) {
        constructor.setAccessible(true);
        Class<?>[] params = constructor.getParameterTypes();
        return params.length == noOfArgs
                && Arrays.stream(params).allMatch(aClass -> aClass.equals(String.class));
    }
}
