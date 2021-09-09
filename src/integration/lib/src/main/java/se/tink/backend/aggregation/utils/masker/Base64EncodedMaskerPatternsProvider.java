package se.tink.backend.aggregation.utils.masker;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches through strings after base64 encoded sensitive strings, masking them in the string if
 * found.
 */
public class Base64EncodedMaskerPatternsProvider implements MaskerPatternsProvider {
    private static final Logger logger =
            LoggerFactory.getLogger(Base64EncodedMaskerPatternsProvider.class);

    private final ImmutableList<Pattern> b64ValuesToMask;

    /**
     * @param sensitiveValuesToMask Collection of plain text values to mask.
     * @throws IllegalArgumentException for sensitive strings of length < 5.
     */
    public Base64EncodedMaskerPatternsProvider(Collection<String> sensitiveValuesToMask) {

        ImmutableList.Builder<Pattern> builder = ImmutableList.builder();
        sensitiveValuesToMask.stream().map(this::generateTargetStrings).forEach(builder::addAll);

        b64ValuesToMask = builder.build();
    }

    @Override
    public ImmutableList<Pattern> getPatternsToMask() {
        return b64ValuesToMask;
    }

    private List<Pattern> generateTargetStrings(final String target) {

        // If target length is < 5 we will end up with cases with no "perfect" b64 substring.
        if (target.length() < 5) {

            logger.debug(
                    "Secret must be length > 5 to be masked by Base64Masker. A shorter string will cause the masker to disregard this string.");
            return Collections.emptyList();
        }

        // Find all perfect b64 substrings by padding the target string and removing the incomplete
        // padding blocks at beginning and end of string.
        String t1 = dropEndPadding(target);
        String t2 = dropEndPadding(target.substring(2));
        String t3 = dropEndPadding(target.substring(1));

        // Resulting substrings will have 'length % 3 == 0' and therefore encode perfectly to b64.
        // These strings can then be used to search blobs of b64 which may contain the target.
        // Padding is added at beginning and end of string to account for the parts we cut away,
        // this is important in order to not leak the beginning/end of the secret.
        List<Pattern> targets = new ArrayList<>(3);
        targets.add(Pattern.compile(Base64.encodeBase64String(t1.getBytes()) + ".{0,2}"));
        targets.add(Pattern.compile(".{2}" + Base64.encodeBase64String(t2.getBytes()) + ".{0,2}"));
        targets.add(Pattern.compile("." + Base64.encodeBase64String(t3.getBytes()) + ".{0,2}"));
        return targets;
    }

    private String dropEndPadding(final String string) {

        int length = string.length();
        int spillOver = length % 3;

        return string.substring(0, length - spillOver);
    }
}
