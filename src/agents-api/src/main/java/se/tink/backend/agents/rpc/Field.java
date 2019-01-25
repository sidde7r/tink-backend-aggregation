package se.tink.backend.agents.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    private String description; // This will be the headline in the view in the app
    private String helpText; // Text displayed just under the input field
    private String hint; // Gray text in the input view giving hint of the format of the input (YYYYMMDDNNNN for Swedish SSN)
    private boolean immutable; // True if the value is immutable
    private boolean masked; // If this is true the input will be masked, like ***** (Should be true for passwords)
    private Integer maxLength; // The maximum length of the input
    private Integer minLength; // The minimum length of the input
    private String name; // The key that will be used to retrieve the input
    private boolean numeric; // True if the input is of numeric type
    private boolean optional; // True if this is an optional input
    private String pattern; // This is a regex pattern of the input ((19|20)[0-9]{10} for Swedish SSN)
    private String patternError; // If the input doesn't match the pattern this is the error message displayed
    private String value; // The value of the input
    private boolean sensitive; // If the field is sensitive. A sensitive field will be stored in the encrypted credential.
    private boolean checkbox; // if the field should be a boolean value displayed as a checkbox
    private String additionalInfo; // This can be used to send additional information, possibly as a serialized JSON.

    public String getDescription() {
        return description;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getHint() {
        return hint;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getPatternError() {
        return patternError;
    }

    public String getValue() {
        return value;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public boolean isMasked() {
        return masked;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public void setImmutable(boolean immutable) {
        this.immutable = immutable; 
    }

    public void setMasked(boolean masked) {
        this.masked = masked;
    }

    public void setMaxLength(Integer length) {
        if (length != null && length == 0) {
            this.maxLength = null;
        } else {
            this.maxLength = length;
        }
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumeric(boolean numeric) {
        this.numeric = numeric;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setPatternError(String patternError) {
        this.patternError = patternError;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public static class Builder {
        private String description;
        private String helpText;
        private String hint;
        private boolean immutable;
        private boolean masked;
        private Integer maxLength;
        private Integer minLength;
        private String name;
        private boolean numeric;
        private boolean optional;
        private String pattern;
        private String patternError;
        private String value;
        private String additionalInfo;
        private boolean checkbox;

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

        public Builder immutable(boolean immutable) {
            this.immutable = immutable;
            return this;
        }

        public Builder masked(boolean masked) {
            this.masked = masked;
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder minLength(int minLength) {
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

        public Field build() {
            Preconditions.checkNotNull(description);
            Preconditions.checkNotNull(name);

            Field field = new Field();

            field.setDescription(description);
            field.setHelpText(helpText);
            field.setHint(hint);
            field.setImmutable(immutable);
            field.setMasked(masked);
            field.setMaxLength(maxLength);
            field.setMinLength(minLength);
            field.setName(name);
            field.setNumeric(numeric);
            field.setOptional(optional);
            field.setPattern(pattern);
            field.setPatternError(patternError);
            field.setValue(value);
            field.setAdditionalInfo(additionalInfo);
            field.setCheckbox(checkbox);

            return field;
        }
    }

    /**
     * Keys for fields that are used in many providers, so that we don't need to do static string coding in all
     * implementations.
     */
    public enum Key {
        HTTP_CLIENT("http-client"),
        SESSION_STORAGE("session-storage"),
        PERSISTENT_STORAGE("persistent-storage"),
        ADDITIONAL_INFORMATION("additionalInformation"),
        PASSWORD("password"),
        PERSISTENT_LOGIN_SESSION_NAME("persistent-login-session"),
        USERNAME("username"),
        ACCESS_TOKEN("access-token"),
        MOBILENUMBER("mobilenumber"),

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
        SIGN_FOR_TRANSFER_INPUT("signfortransferinput");

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

    /**
     * Validate the value of a field
     * @param field
     * @return true if valid (false otherwise)
     */
    @JsonIgnore
    public static boolean isValidField(Field field) {
        return isValidField(field, field.getValue());
    }

    /**
     * Validate the value of a field
     * @param field
     * @param value
     * @return true if valid (false otherwise)
     */
    @JsonIgnore
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
