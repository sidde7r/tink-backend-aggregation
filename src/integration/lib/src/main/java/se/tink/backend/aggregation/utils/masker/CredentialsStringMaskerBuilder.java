package se.tink.backend.aggregation.utils.masker;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.libraries.serialization.utils.JsonFlattener;

public class CredentialsStringMaskerBuilder implements StringMaskerBuilder {
    private final Credentials credentials;
    private final Iterable<CredentialsProperty> maskedProperties;
    private final ImmutableList<String> propertyValuesToMask;

    private static final List<CredentialsProperty> CREDENTIALS_DEFAULT_MASKED_PROPERTIES =
            ImmutableList.of(
                    CredentialsStringMaskerBuilder.CredentialsProperty.PASSWORD,
                    CredentialsStringMaskerBuilder.CredentialsProperty.SECRET_KEY,
                    CredentialsStringMaskerBuilder.CredentialsProperty.SENSITIVE_PAYLOAD,
                    CredentialsStringMaskerBuilder.CredentialsProperty.USERNAME);

    public CredentialsStringMaskerBuilder(Credentials credentials) {
        this(credentials, CREDENTIALS_DEFAULT_MASKED_PROPERTIES);
    }

    public CredentialsStringMaskerBuilder(
            Credentials credentials, Iterable<CredentialsProperty> maskedProperties) {
        this.credentials = credentials;
        this.maskedProperties = maskedProperties;
        this.propertyValuesToMask = getNonEmptyPropertyValuesToMask();
    }

    @Override
    public ImmutableList<Pattern> getValuesToMask() {
        return propertyValuesToMask.stream()
                .map(s -> Pattern.compile(s, Pattern.LITERAL))
                .collect(ImmutableList.toImmutableList());
    }

    private ImmutableList<String> getNonEmptyPropertyValuesToMask() {
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

        // Add url encoded versions of all strings
        List<String> escapedValues =
                valuesToMask.stream()
                        .map(this::getEscapedString)
                        .filter(s -> !valuesToMask.contains(s))
                        .collect(Collectors.toList());

        valuesToMask.addAll(escapedValues);
        return ImmutableList.sortedCopyOf(
                MaskingConstants.SENSITIVE_VALUES_SORTING_COMPARATOR, valuesToMask);
    }

    private String getEscapedString(final String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
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
            case SENSITIVE_PAYLOAD:
                break;
            case SECRET_KEY:
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
        Map<String, String> sensitivePayload = credentials.getSensitivePayloadAsMap();
        Collection<String> sensitiveValues = new ArrayList<>();

        if (sensitivePayload == null) {
            return ImmutableList.of();
        } else {
            sensitivePayload.forEach(
                    (key, value) -> {
                        if (Strings.isNullOrEmpty(value)) {
                            return;
                        }
                        if (Key.PERSISTENT_STORAGE.getFieldKey().equalsIgnoreCase(key)
                                || Key.SESSION_STORAGE.getFieldKey().equalsIgnoreCase(key)) {
                            sensitiveValues.addAll(deserializeMapValues(sensitivePayload.get(key)));
                        } else {
                            sensitiveValues.add(sensitivePayload.get(key));
                        }
                    });
            return sensitiveValues;
        }
    }

    private Collection<? extends String> deserializeMapValues(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return Collections.emptyList();
        }
        Map<String, String> map;
        try {
            map = JsonFlattener.flattenJsonToMap(s);
        } catch (IOException e) {
            throw new IllegalStateException("Could not deserialize storage.", e);
        }

        return map.values().stream()
                .filter(string -> !Strings.isNullOrEmpty(string))
                .collect(Collectors.toList());
    }

    public enum CredentialsProperty {
        PASSWORD,
        USERNAME,
        SENSITIVE_PAYLOAD,
        SECRET_KEY
    }
}
