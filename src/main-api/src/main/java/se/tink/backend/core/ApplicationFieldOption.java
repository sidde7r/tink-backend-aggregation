package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;

public class ApplicationFieldOption {
    @Tag(1)
    @ApiModelProperty(name = "value", value="The value to give the Field if this Option was chosen", example = "apartment")
    private String value;
    @Tag(2)
    @ApiModelProperty(name = "label", value="The label of this Option", example = "Apartment")
    private String label;
    @Tag(3)
    @ApiModelProperty(name = "description", value="A description of the Option", example = "null")
    private String description;
    @Tag(4)
    @JsonProperty(value = "payload")
    @ApiModelProperty(name = "payload", value="A payload can be given in order to layout custom views", example = "null")
    private String serializedPayload;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSerializedPayload() {
        return serializedPayload;
    }

    public void setSerializedPayload(String serializedPayload) {
        this.serializedPayload = serializedPayload;
    }
    
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .add("label", label)
                .add("description", description)
                .add("serializedPayload", serializedPayload)
                .toString();
    }
}
