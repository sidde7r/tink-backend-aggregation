package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Field {
    private String description; // This will be the headline in the view in the app
    private String group; // Identifies fields which should be rendered in one group
    private String helpText; // Text displayed just under the input field
    private String hint; // Gray text in the input view giving hint of the format of the input
    // (YYYYMMDDNNNN for Swedish SSN)
    private boolean immutable; // True if the value is immutable
    private boolean
            masked; // If this is true the input will be masked, like ***** (Should be true for
    // passwords)
    private Integer maxLength; // The maximum length of the input
    private Integer minLength; // The minimum length of the input
    private String name; // The key that will be used to retrieve the input
    private boolean numeric; // True if the input is of numeric type
    private boolean oneOf; // Identifies if only one field within group should be filled.
    private boolean optional; // True if this is an optional input
    private String
            pattern; // This is a regex pattern of the input ((19|20)[0-9]{10} for Swedish SSN)
    private String patternError; // If the input doesn't match the pattern this is the error message
    // displayed
    private String value; // The value of the input
    private boolean sensitive; // If the field is sensitive. A sensitive field will be stored in the
    // encrypted credential.
    private String style; // Information about style of 2FA screen.
    private String type; // Type of field
    private boolean checkbox; // if the field should be a boolean value displayed as a checkbox
    private String additionalInfo; // This can be used to send additional information, possibly as a
    // serialized JSON.
    private List<SelectOption> selectOptions;

    public void setValue(String value) {
        this.value = value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String description;
        private String group;
        private String helpText;
        private String hint;
        private boolean immutable;
        private boolean masked;
        private Integer maxLength;
        private Integer minLength;
        private String name;
        private boolean numeric;
        private boolean oneOf;
        private boolean optional;
        private String pattern;
        private String patternError;
        private String style;
        private String type;
        private String value;
        private String additionalInfo;
        private boolean checkbox;
        private boolean sensitive;
        private List<SelectOption> selectOptions;

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder helpText(String helpText) {
            this.helpText = helpText;
            return this;
        }

        public Builder hint(String hint) {
            this.hint = hint;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder immutable(boolean immutable) {
            this.immutable = immutable;
            return this;
        }

        public Builder masked(boolean masked) {
            this.masked = masked;
            return this;
        }

        public Builder maxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder minLength(Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder numeric(boolean numeric) {
            this.numeric = numeric;
            return this;
        }

        public Builder oneOf(boolean oneOf) {
            this.oneOf = oneOf;
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder patternError(String patternError) {
            this.patternError = patternError;
            return this;
        }

        public Builder sensitive(boolean sensitive) {
            this.sensitive = sensitive;
            return this;
        }

        public Builder style(String style) {
            this.style = style;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder additionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        public Builder checkbox(boolean checkbox) {
            this.checkbox = checkbox;
            return this;
        }

        public Builder selectOptions(List<SelectOption> selectOptions) {
            this.selectOptions = selectOptions;
            return this;
        }

        public Builder selectOptions(SelectOption... selectOptions) {
            this.selectOptions = Arrays.asList(selectOptions);
            return this;
        }

        public Field build() {
            Preconditions.checkNotNull(description);
            Preconditions.checkNotNull(name);

            Field field = new Field();

            field.description = description;
            field.group = group;
            field.helpText = helpText;
            field.hint = hint;
            field.immutable = immutable;
            field.masked = masked;
            if (maxLength != null && maxLength == 0) {
                field.maxLength = null;
            } else {
                field.maxLength = maxLength;
            }
            field.minLength = minLength;
            field.name = name;
            field.numeric = numeric;
            field.oneOf = oneOf;
            field.optional = optional;
            field.pattern = pattern;
            field.patternError = patternError;
            field.sensitive = sensitive;
            field.style = style;
            field.type = type;
            field.value = value;
            field.additionalInfo = additionalInfo;
            field.checkbox = checkbox;
            if (selectOptions != null) {
                field.selectOptions = Collections.unmodifiableList(selectOptions);
            }

            return field;
        }
    }

    /**
     * Keys for fields that are used in many providers, so that we don't need to do static string
     * coding in all implementations.
     */
    public enum Key {
        HTTP_API_CLIENT("http-api-client"),
        HTTP_CLIENT("http-client"),
        SESSION_STORAGE("session-storage"),
        PERSISTENT_STORAGE("persistent-storage"),
        ADDITIONAL_INFORMATION("additionalInformation"),
        PASSWORD("password"),
        PERSISTENT_LOGIN_SESSION_NAME("persistent-login-session"),
        USERNAME("username"),
        ACCESS_TOKEN("access-token"),
        MOBILENUMBER("mobilenumber"),
        CORPORATE_ID("psu-corporate-id"),
        NATIONAL_ID_NUMBER("national-id-number"),
        DATE_OF_BIRTH("date-of-birth"),
        SECURITY_NUMBER("security-number"),

        // Supplemental field names.
        ADD_BENEFICIARY_INPUT("addbeneficiaryinput"),
        LOGIN_DESCRIPTION("logindescription"),
        LOGIN_INPUT("logininput"),
        OTP_INPUT("otpinput"),
        SIGN_CODE_DESCRIPTION("signcodedescription"),
        SIGN_CODE_INPUT("signcodeinput"),
        SIGN_FOR_BENEFICIARY_DESCRIPTION("signforbeneficiarydescription"),
        SIGN_FOR_BENEFICIARY_EXTRA_DESCRIPTION("signforbeneficiaryextradescription"),
        SIGN_FOR_BENEFICIARY_INPUT("signforbeneficiaryinput"),
        SIGN_FOR_TRANSFER_DESCRIPTION("signfortransferdescription"),
        SIGN_FOR_TRANSFER_EXTRA_DESCRIPTION("signfortransferextradescription"),
        SIGN_FOR_TRANSFER_INPUT("signfortransferinput"),
        ACCESS_PIN("accesspininput"),
        ACCESS_PIN_CONFIRMATION("confirmaccesspininput"),
        EMAIL("email"),
        IBAN("iban"),
        BANKID_PASSWORD("bankid-password"),
        SESSION_EXPIRY_TIME("sessionExpiryTime");

        private final String fieldKey;

        Key(String fieldKey) {
            this.fieldKey = fieldKey;
        }

        public String getFieldKey() {
            return fieldKey;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name())
                    .add("fieldKey", fieldKey)
                    .toString();
        }
    }
}
