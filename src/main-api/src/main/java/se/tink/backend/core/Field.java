package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

    @Exclude
    private String defaultValue;
    @Tag(1)
    private String description;
    @Exclude
    private boolean exposed = true;
    @Tag(15)
    private List<Field> children;
    @Tag(13)
    private String helpText;
    @Tag(2)
    private String hint;
    @Tag(7)
    private boolean immutable;
    @Tag(5)
    private boolean masked;
    @Tag(3)
    private Integer maxLength;
    @Tag(4)
    private Integer minLength;
    @Tag(9)
    private String name;
    @Tag(6)
    private boolean numeric;
    @Tag(8)
    private boolean optional;
    @Tag(16)
    private List<String> options;
    @Tag(11)
    private String pattern;
    @Tag(12)
    private String patternError;
    @Tag(14)
    private String type;
    @Tag(10)
    private String value;
    @Exclude
    private boolean sensitive;
    @Tag(17)
    private boolean checkbox;
    @Tag(18)
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

    @ApiModelProperty(name = "helpText", value = "Text displayed next to the input field", example = "Enter your username")
    public String getHelpText() {
        return helpText;
    }

    @ApiModelProperty(name = "hint", value = "Gray text in the input view (Similar to a placeholder)", example = "YYYYMMDD-NNNN")
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

    @ApiModelProperty(name = "options", value = "A list of options where the user should select one")
    public List<String> getOptions() {
        return options;
    }

    public String getPattern() {
        return pattern;
    }

    public String getPatternError() {
        return patternError;
    }

    @ApiModelProperty(hidden = true)
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

    @ApiModelProperty(name = "masked", value = "Controls whether or not the field should be shown masked, like a password field")
    public boolean isMasked() {
        return masked;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public void setChildren(List<Field> children) {
        this.children = children;
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

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setPatternError(String patternError) {
        this.patternError = patternError;
    }

    public void setType(String type) {
        this.type = type;
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

    @ApiModelProperty(name = "checkbox", value = "Display boolean value as checkbox")
    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    @ApiModelProperty(name = "additionalInfo", value = "A serialized JSON containing additional information that could be useful")
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Keys for fields that are used in many providers, so that we don't need to do static string coding in all
     * implementations.
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
