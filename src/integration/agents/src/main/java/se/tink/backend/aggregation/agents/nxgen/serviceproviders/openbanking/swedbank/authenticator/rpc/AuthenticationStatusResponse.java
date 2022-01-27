package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.entities.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationStatusResponse {
    private String scaStatus;
    private String authorizationCode;
    private List<TppMessagesEntity> tppMessages;

    public String getScaStatus() {
        return scaStatus;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    @JsonIgnore
    public boolean loginCanceled() {
        return ListUtils.emptyIfNull(tppMessages).stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.USER_CANCEL.equalsIgnoreCase(tppMessage.getCode()));
    }
}
