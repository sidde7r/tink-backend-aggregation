package se.tink.libraries.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        this.value = value;
    }

    @Override
    public String toString() {
        return "RemittanceInformation{" + "type=" + type + ", value='" + value + '\'' + '}';
    }
}
