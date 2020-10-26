package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.entities.TppMessagesEntity;
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
        return Optional.ofNullable(tppMessages).orElseGet(Lists::newArrayList).stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.USER_CANCEL.equalsIgnoreCase(tppMessage.getCode()));
    }
}
