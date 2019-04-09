package se.tink.backend.aggregation.utils;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;

public class CredentialsStringMasker implements StringMasker {
    private static final String MASK = "***MASKED***";

    private final Credentials credentials;
    private final Iterable<CredentialsProperty> maskedProperties;

    public CredentialsStringMasker(
            Credentials credentials, Iterable<CredentialsProperty> maskedProperties) {
        this.credentials = credentials;
        this.maskedProperties = maskedProperties;
    }

    @Override
    public String getMasked(String string) {
        Set<String> propertyValuesToMask = getNonEmptyPropertyValuesToMask();

        for (String value : propertyValuesToMask) {
            string = string.replace(value, MASK);
        }

        return string;
    }

    private Set<String> getNonEmptyPropertyValuesToMask() {
        Set<String> valuesToMask = Sets.newHashSet();

        for (CredentialsProperty property : maskedProperties) {
            switch (property) {
                case SENSITIVE_PAYLOAD:
                    valuesToMask.addAll(getSensitivePayloadValuesNotEmpty());
                    break;
                default:
                    Optional<String> propertyValue = getNonEmptyPropertyValue(property);
                    if (propertyValue.isPresent()) {
                        valuesToMask.add(propertyValue.get());
                    }
                    break;
            }
        }

        return valuesToMask;
    }

    private Optional<String> getNonEmptyPropertyValue(CredentialsProperty property) {
        String value = null;

        switch (property) {
            case PASSWORD:
                value = credentials.getPassword();
                break;
            case USERNAME:
                value = credentials.getUsername();
                break;
            default:
                break;
        }

        if (!Strings.isNullOrEmpty(value)) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    private Collection<String> getSensitivePayloadValuesNotEmpty() {
        Map<String, String> sensitivePayload = credentials.getSensitivePayload();

        if (sensitivePayload == null) {
            return ImmutableList.of();
        } else {
            return FluentIterable.from(sensitivePayload.values())
                    .filter(Predicates.notNull())
                    .filter(Predicates.not(Predicates.equalTo("")))
                    .toSet();
        }
    }

    public enum CredentialsProperty {
        PASSWORD,
        USERNAME,
        SENSITIVE_PAYLOAD,
        SECRET_KEY
    }
}
