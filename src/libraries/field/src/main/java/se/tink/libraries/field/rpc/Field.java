package se.tink.libraries.field.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    private boolean oneOf;
    private boolean numeric;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    @ApiModelProperty(name = "children", value = "Child fields to this field")
    public List<Field> getChildren() {
        return children;
    }

    @ApiModelProperty(name = "group", value = "It identifies to which group this field belongs.")
    public String getGroup() {
        return group;
    }

    @ApiModelProperty(
            name = "helpText",
            value = "Text displayed next to the input field",
            example = "Enter your username")
    public String getHelpText() {
        return helpText;
    }

    @ApiModelProperty(
            name = "hint",
            value = "Gray text in the input view (Similar to a placeholder)",
            example = "YYYYMMDD-NNNN")
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

    @ApiModelProperty(
            name = "options",
            value = "A list of options where the user should select one")
    public List<String> getOptions() {
        return options;
    }

    public String getPattern() {
        return pattern;
    }

    public String getPatternError() {
        return patternError;
    }

    @ApiModelProperty(name = "style", value = "Information about style of 2FA screen.")
    public String getStyle() {
        return style;
    }

    @ApiModelProperty(name = "type", value = "Stores information about field type")
    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @ApiModelProperty(hidden = true)
    public boolean isExposed() {
        return exposed;
    }

    public boolean isImmutable() {
        return immutable;
    }

    @ApiModelProperty(
            name = "masked",
            value =
                    "Controls whether or not the field should be shown masked, like a password field")
    public boolean isMasked() {
        return masked;
    }

    public boolean isNumeric() {
        return numeric;
    }

    @ApiModelProperty(
            name = "oneOf",
            value = "Identifies if only one field within group should be filled.")
    public boolean isOneOf() {
        return oneOf;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setMaxLength(Integer length) {
        if (length != null && length == 0) {
            this.maxLength = null;
        } else {
            this.maxLength = length;
        }
    }

    public boolean isSensitive() {
        return sensitive;
    }

    @ApiModelProperty(name = "checkbox", value = "Display boolean value as checkbox")
    public boolean isCheckbox() {
        return checkbox;
    }

    @ApiModelProperty(
            name = "additionalInfo",
            value = "A serialized JSON containing additional information that could be useful")
    public String getAdditionalInfo() {
        return additionalInfo;
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
