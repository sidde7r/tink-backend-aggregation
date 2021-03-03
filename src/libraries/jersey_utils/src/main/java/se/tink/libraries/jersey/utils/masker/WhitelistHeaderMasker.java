package se.tink.libraries.jersey.utils.masker;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class WhitelistHeaderMasker {

    private final ImmutableSet<String> whiteListedHeaderKeys;

    public WhitelistHeaderMasker(ImmutableSet<String> whiteListedHeaderKeys) {
        this.whiteListedHeaderKeys = whiteListedHeaderKeys;
    }

    public List<String> mask(String headerKey, Collection<?> headerValue) {

        if (whiteListedHeaderKeys.contains(headerKey)) {
            return headerValue.stream().map(Object::toString).collect(Collectors.toList());
        }

        return Collections.nCopies(headerValue.size(), "***Masked***");
    }
}
