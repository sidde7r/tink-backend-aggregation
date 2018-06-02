package se.tink.backend.consent.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.backend.consent.core.UserConsent;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class UserConsentListResponse {
    @ApiModelProperty(name = "consents", value = "The consents the user have accepted or declined.")
    @Tag(1)
    private List<UserConsent> consents;

    public UserConsentListResponse(List<UserConsent> consents) {
        this.consents = consents;
    }

    public List<UserConsent> getConsents() {
        return consents;
    }

    public void setConsents(List<UserConsent> consents) {
        this.consents = consents;
    }
}
