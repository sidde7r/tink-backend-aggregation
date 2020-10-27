package se.tink.libraries.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.libraries.transfer.enums.RemittanceInformationType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemittanceInformation {
    @JsonProperty private RemittanceInformationType type;
    @JsonProperty private String value;

    public RemittanceInformationType getType() {
        return type;
    }

    public void setType(RemittanceInformationType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        Preconditions.checkNotNull(value, "The value for remittance information is missing.");
        this.value = value;
    }

    @JsonIgnore
    public boolean isOfType(RemittanceInformationType type) {
        return type.equals(this.type);
    }

    @Override
    @JsonIgnore
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RemittanceInformation)) {
            return false;
        }

        RemittanceInformation otherRemittanceInformation = (RemittanceInformation) other;
        return this.isOfType(otherRemittanceInformation.getType())
                && this.value.equals(otherRemittanceInformation.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "RemittanceInformation{" + "type=" + type + ", value='" + value + '\'' + '}';
    }
}
