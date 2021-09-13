package se.tink.backend.aggregation.utils.masker;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class Base64DecodedMaskerValuesProvider implements MaskerPatternsProvider {

    private final ImmutableList<Pattern> b64DecodedValuesToMask;

    public Base64DecodedMaskerValuesProvider(Collection<String> sensitiveValuesToMask) {

        ImmutableList.Builder<Pattern> builder = ImmutableList.builder();
        sensitiveValuesToMask.stream()
                .map(this::generatePattern)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(builder::add);

        b64DecodedValuesToMask = builder.build();
    }

    private Optional<Pattern> generatePattern(final String value) {
        byte[] decodedValue = Base64.decodeBase64(value);
        if (Objects.isNull(decodedValue) || decodedValue.length == 0) {
            return Optional.empty();
        }
        return Optional.of(Pattern.compile(new String(decodedValue)));
    }

    @Override
    public ImmutableList<Pattern> getPatternsToMask() {
        return b64DecodedValuesToMask;
    }
}
