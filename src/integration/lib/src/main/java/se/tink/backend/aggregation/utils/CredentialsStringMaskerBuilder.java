package se.tink.backend.aggregation.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.libraries.serialization.utils.JsonFlattener;

public class CredentialsStringMaskerBuilder implements StringMaskerBuilder {
    private final Credentials credentials;
    private final Iterable<CredentialsProperty> maskedProperties;
    private final ImmutableList<String> propertyValuesToMask;

    public CredentialsStringMaskerBuilder(
            Credentials credentials, Iterable<CredentialsProperty> maskedProperties) {
        this.credentials = credentials;
        this.maskedProperties = maskedProperties;
        this.propertyValuesToMask = getNonEmptyPropertyValuesToMask();
    }

    @Override
    public ImmutableList<String> getValuesToMask() {
        return ImmutableList.copyOf(propertyValuesToMask);
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

        return ImmutableList.sortedCopyOf(
                LogMasker.SENSITIVE_VALUES_SORTING_COMPARATOR, valuesToMask);
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
        Map<String, String> sensitivePayload = credentials.getSensitivePayload();
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
            map =
                    JsonFlattener.flattenJsonToMap(
                            JsonFlattener.ROOT_PATH, new ObjectMapper().readTree(s));
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
