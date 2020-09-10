package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.entities.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericResponse {
    private List<TppMessagesEntity> tppMessages;

    public List<TppMessagesEntity> getTppMessages() {
        return Optional.of(tppMessages).orElseGet(Lists::newArrayList);
    }

    @JsonIgnore
    public boolean isKycError() {
        return getTppMessages().stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.KYC_INVALID.equalsIgnoreCase(tppMessage.getCode())
                                        || ErrorCodes.MISSING_BANK_AGREEMENT.equalsIgnoreCase(
                                                tppMessage.getCode()));
    }

    @JsonIgnore
    public boolean requiresSca() {
        return getTppMessages().stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.SCA_REQUIRED.equalsIgnoreCase(tppMessage.getCode()));
    }

    @JsonIgnore
    public boolean refreshTokenHasExpired() {
        return getTppMessages().stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.REFRESH_TOKEN_EXPIRED.equalsIgnoreCase(
                                        tppMessage.getText()));
    }

    @JsonIgnore
    public boolean hasWrongUserId() {
        return getTppMessages().stream()
                .anyMatch(
                        tppMessage ->
                                ErrorCodes.WRONG_USER_ID.equalsIgnoreCase(tppMessage.getText()));
    }
}
