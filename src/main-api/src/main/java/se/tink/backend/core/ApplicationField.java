package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.core.enums.ApplicationFieldType;

public class ApplicationField implements Cloneable {
    @Tag(1)
    @ApiModelProperty(name = "defaultValue", value = "The default value to set if no value has been given", example = "null")
    private String defaultValue;
    @Tag(2)
    @ApiModelProperty(name = "description", value = "A description for this field", example = "null")
    private String description;
    @Exclude
    @JsonIgnore
    private List<ApplicationFieldError> errors;
    @Tag(3)
    @JsonProperty(value = "error")
    @ApiModelProperty(name = "error", value = "A message if the Field did not validate, null if OK.", example = "null")
    private String displayError;
    @Tag(4)
    @ApiModelProperty(name = "label", value = "The label of this Field", example = "Bostadstyp")
    private String label;
    @Tag(5)
    @ApiModelProperty(name = "name", value = "A unique identifier on a Field within this ApplicationForm", example = "real-estate-type", required = true)
    private String name;
    @Tag(6)
    @ApiModelProperty(name = "options", value = "The default value of this field if value is null. Only used for type select")
    private List<ApplicationFieldOption> options;
    @Tag(7)
    @ApiModelProperty(name = "pattern", value = "A regular expression that can be used to quickly validate input on the client side", example = ".+")
    private String pattern;
    @Tag(8)
    @ApiModelProperty(name = "type", value = "The type of this Field", example = "select", allowableValues = ApplicationFieldType.DOCUMENTED)
    private String type;
    @Tag(9)
    @ApiModelProperty(name = "value", value = "The value of this field.", example = "null", required = true)
    private String value;
    @Tag(10)
    @ApiModelProperty(name = "required", value = "If this field is required or not", example = "true")
    private boolean required;
    @Tag(11)
    @ApiModelProperty(name = "readonly", value = "Specifies if a user can make changes to this field", example = "true")
    private boolean readOnly;
    @Tag(12)
    @ApiModelProperty(name = "dependency", value = "Tells if a field has a parent field")
    private String dependency;
    @Tag(13)
    @ApiModelProperty(name = "infoTitle", value = "If a description is not enough, an info section can be supplied. This is the title of such section.")
    private String infoTitle;
    @Tag(14)
    @ApiModelProperty(name = "infoBody", value = "If a description is not enough, an info section can be supplied. This is the body of such section.")
    private String infoBody;
    @Tag(15)
    @ApiModelProperty(name = "introduction", value = "Some fields require a short introduction to describe the context.")
    private String introduction;

    @Override
    public ApplicationField clone() throws CloneNotSupportedException {
        return (ApplicationField) super.clone();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayError() {
        return displayError;
    }

    public void setDisplayError(String displayError) {
        this.displayError = displayError;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonIgnore
    public String getTemplateName() {
        if (name.contains("|")) {
            return name.substring(0, name.indexOf('|'));
        }
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApplicationFieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<ApplicationFieldOption> options) {
        this.options = options;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public ApplicationFieldType getType() {
        if (type == null) {
            return null;
        } else {
            return ApplicationFieldType.valueOf(type);
        }
    }

    public void setType(ApplicationFieldType type) {
        if (type == null) {
            this.type = null;
        } else {
            this.type = type.toString();
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ApplicationFieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<ApplicationFieldError> errors) {
        this.errors = errors;
    }

    public boolean hasError() {
        return errors != null && errors.size() > 0;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }
    
    public String getInfoTitle() {
        return infoTitle;
    }

    public void setInfoTitle(String infoTitle) {
        this.infoTitle = infoTitle;
    }
    
    public String getInfoBody() {
        return infoBody;
    }

    public void setInfoBody(String infoBody) {
        this.infoBody = infoBody;
    }
    
    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("name", name)
                .add("label", label)
                .add("description", description)
                .add("options", options)
                .add("value", value)
                .add("defaultValue", defaultValue)
                .toString();
    }
}
