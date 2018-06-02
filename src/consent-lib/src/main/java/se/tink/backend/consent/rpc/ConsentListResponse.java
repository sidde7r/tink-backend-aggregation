package se.tink.backend.consent.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.consent.core.Consent;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class ConsentListResponse {
    @ApiModelProperty(name = "consents", value = "The available consents.")
    @Tag(1)
    private List<Consent> consents;

    public ConsentListResponse(List<Consent> consents) {
        this.consents = consents;
    }

    public List<Consent> getConsents() {
        return consents;
    }
}
