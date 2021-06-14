package se.tink.backend.aggregation.client.provider_configuration.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
@EqualsAndHashCode
@Setter
public class Field {

    private String defaultValue;
    private String description;
    private boolean exposed = true;
    private List<Field> children;
    private String group;
    private String helpText;
    private String hint;
    private boolean immutable;
    private boolean masked;

    @Setter(AccessLevel.NONE)
    private Integer maxLength;

    private Integer minLength;
    private String name;
    private boolean numeric;
    private boolean oneOf;
    private boolean optional;
    private List<String> options;
    private String pattern;
    private String patternError;
    private String style;
    private String type;
    private String value;
    private boolean sensitive;
    private boolean checkbox;
    private String additionalInfo;
    private List<SelectOption> selectOptions;

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public List<Field> getChildren() {
        return children;
    }

    public String getGroup() {
        return group;
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

    public List<String> getOptions() {
        return options;
    }

    public String getPattern() {
        return pattern;
    }

    public String getPatternError() {
        return patternError;
    }

    public String getStyle() {
        return style;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isExposed() {
        return exposed;
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

    public boolean isOneOf() {
        return oneOf;
    }

    public boolean isOptional() {
        return optional;
    }

    public List<SelectOption> getSelectOptions() {
        return selectOptions;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setMaxLength(Integer length) {
        if (length != null && length == 0) {
            this.maxLength = null;
        } else {
            this.maxLength = length;
        }
    }

    /**
     * Keys for fields that are used in many providers, so that we don't need to do static string
     * coding in all implementations.
     */
    public enum Key {
        HTTP_CLIENT("http-client"),
        SESSION_STORAGE("session-storage"),
        ADDITIONAL_INFORMATION("additionalInformation"),
        PASSWORD("password"),
        PERSISTENT_LOGIN_SESSION_NAME("persistent-login-session"),
        USERNAME("username"),
        ACCESS_TOKEN("access-token");

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
