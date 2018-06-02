package se.tink.backend.rpc;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;

public class SupplementalInformation {
    @Tag(1)
    @ApiModelProperty(name = "information", value = "A key-value structure, use \"name\":\"value\" from the fields found in supplementalInformation on the Credentials when status is AWAITING_SUPPLEMENTAL_INFORMATION.", example = "{\"code\":\"123456\", \"name2\":\"value2\"}")
    private Map<String, String> information;

    public Map<String, String> getInformation() {
        return information;
    }

    public void setInformation(Map<String, String> information) {
        this.information = information;
    }
}
