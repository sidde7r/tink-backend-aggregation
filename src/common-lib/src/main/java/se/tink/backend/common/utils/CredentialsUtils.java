package se.tink.backend.common.utils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.Provider;

public class CredentialsUtils {
    public static final String BANK_ID_PROVIDER_MATCHER = "-bankid";
    public static final int MAXIMUM_BANKID_CREDENTIALS_MANUAL_STALENESS_MS = 5 * 1000; // 5 seconds
    public static final int MAXIMUM_CREDENTIALS_TEMP_ERROR_STALENESS_MS = 60 * 60 * 1000;
    public static final int MAXIMUM_SWEDISH_CREDENTIALS_MANUAL_STALENESS_MS = 10 * 60 * 1000; // 10 minutes

    /**
     * Checks if the credentials has updated any non-masked fields from the client.
     * 
     * @param provider
     * @param existingCredentials
     * @param credentials
     * @return
     */
    public static boolean isValidCredentialsUpdate(Provider provider, Credentials existingCredentials,
            Credentials credentials) {

        if (provider == null) {
            return false;
        }

        Map<String, String> oldFields = existingCredentials.getFields();
        Map<String, String> newFields = credentials.getFields();

        Iterable<String> maskedAndSensitiveFields = getMaskedAndSensitiveFieldsForProvider(provider);

        for (String fieldKey : newFields.keySet()) {
            // Cannot validate the masked fields.

            if (Iterables.contains(maskedAndSensitiveFields, fieldKey)) {
                continue;
            }

            // If there is a new field, return error.

            if (!oldFields.containsKey(fieldKey)) {
                return false;
            }

            String oldFieldValue = oldFields.get(fieldKey);

            // If a non-masked field has changed, return error.

            if (!Objects.equal(newFields.get(fieldKey), oldFieldValue) && existingCredentials.getUpdated() != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the new credentials is already registered with same non masked fields (username) for the "same" provider
     * and user. "same" meaning 'nordnet' and 'nordnet-bankid' is same provider.
     * 
     * @param provider
     * @param preexistingId
     *            if existing credential is being modified, the id of it. Absent if credential is created.
     * @param createCredentials
     * @param credentialsForSameProvider
     * @return
     */
    public static boolean isSameAsExistingCredentials(Provider provider, Optional<String> preexistingId,
            Credentials createCredentials, Iterable<Credentials> credentialsForSameProvider) {
        
        Preconditions.checkArgument(createCredentials.getProviderName().equals(provider.getName()));

        Map<String, String> existingCredentialsFields = createCredentials.getFields();

        // loop credentials and check if any non masked field value is the same
        // as the old credentials

        for (Credentials c : credentialsForSameProvider) {

            Preconditions.checkArgument(c.getProviderName().replace(BANK_ID_PROVIDER_MATCHER, "")
                    .equals(provider.getName().replace(BANK_ID_PROVIDER_MATCHER, "")));

            if (preexistingId.isPresent() && c.getId().equals(preexistingId.get())) {
                // Since URI id:s have higher priority over PUT body id:s, we are using that one
                // here. Also the web client does not submit an id in the request body...

                continue;
            }
            
            Map<String, String> credentialsFields = c.getFields();

            boolean allFieldsEqual = true;
            for (Field providerField : provider.getFields()) {
                if (providerField.isMasked()) {
                    continue;
                }

                if (credentialsFields.get(providerField.getName()) != null
                        && existingCredentialsFields.get(providerField.getName()) != null) {
                    if (!credentialsFields.get(providerField.getName()).equals(
                            existingCredentialsFields.get(providerField.getName()))) {
                        allFieldsEqual = false;
                        // Could potentially add continue block here for the outer for loop. Not doing it to keep things
                        // simple.
                    }
                }
            }

            if (allFieldsEqual) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists all masked fields for a provider
     * 
     * @param provider
     * @return
     */
    public static Iterable<String> getMaskedAndSensitiveFieldsForProvider(Provider provider) {
        return Iterables.transform(
                provider.getFields().stream()
                        .filter(field -> field.isMasked() || field.isSensitive())
                        .collect(Collectors.toList()), Field::getName);
    }

    /**
     * Validate that the fields are correct.
     * 
     * @param credentials
     * @param provider
     */
    public static boolean isValidCredentials(Credentials credentials, Provider provider) {
        if (provider == null) {
            return false;
        }

        Map<String, String> fields = credentials.getFields();

        for (Field field : provider.getFields()) {
            if (!isValidField(field, fields.get(field.getName()))) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Validate the value of a field
     * @param field
     * @return true if valid (false otherwise)
     */
    public static boolean isValidField(Field field) {
        return isValidField(field, field.getValue());
    }
    
    /**
     * Validate the value of a field
     * @param field
     * @param value
     * @return true if valid (false otherwise)
     */
    public static boolean isValidField(Field field, String value) {

        if (Strings.isNullOrEmpty(value)) {
            return field.isOptional();
        }

        if (field.getMinLength() != null && field.getMinLength() > value.length()) {
            return false;
        }

        if (field.getMaxLength() != null && field.getMaxLength() > 0 && field.getMaxLength() < value.length()) {
            return false;
        }

        if (!Strings.isNullOrEmpty(field.getPattern())) {
            return Pattern.matches(field.getPattern(), value);
        }
        
        return true;
    }
}
