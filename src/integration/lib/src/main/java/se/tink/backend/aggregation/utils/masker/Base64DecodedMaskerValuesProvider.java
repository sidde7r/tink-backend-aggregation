package se.tink.backend.aggregation.utils.masker;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Base64DecodedMaskerValuesProvider implements MaskerPatternsProvider {

    private final ImmutableList<Pattern> b64DecodedValuesToMask;

    public Base64DecodedMaskerValuesProvider(Collection<String> sensitiveValuesToMask) {

        ImmutableList.Builder<Pattern> builder = ImmutableList.builder();
        sensitiveValuesToMask.stream().map(this::generateTargetStrings).forEach(builder::addAll);

        b64DecodedValuesToMask = builder.build();
    }

    private List<Pattern> generateTargetStrings(final String target) {

        try {
            String decodedString = new String(Base64.decodeBase64(target));
            return Collections.singletonList(Pattern.compile(decodedString));
        } catch (Exception e) {
            // NOOP intentionally
        }
        return Collections.emptyList();
    }

    @Override
    public ImmutableList<Pattern> getPatternsToMask() {
        return b64DecodedValuesToMask;
    }
}
