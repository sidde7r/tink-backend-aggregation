package se.tink.backend.common.utils;

import com.google.common.base.Function;

/**
 * Transforms a string to add quotation marks around it. Utility class that makes it more compact to properly log optionals:
 * 
 * <code>
 * log.info("Bad user with userAgent '" + userAgent.or("<missing>") + "'.");
 * </code>
 * will not properly distinguish between missing values and the value "<missing>". This can be important in forensics.
 * This utility class makes it easier to solve the above issue:
 * <code>
 * log.info("Bad user with userAgent " + userAgent.transform(new QuotationMarker()).or("<missing>") + ".");
 * </code>
 */
public class QuotationMarker implements Function<String, String> {

    private static final String TEMPLATE = "'%s'";
    
    @Override
    public String apply(String input) {
        return String.format(TEMPLATE, input);
    }
    
}
