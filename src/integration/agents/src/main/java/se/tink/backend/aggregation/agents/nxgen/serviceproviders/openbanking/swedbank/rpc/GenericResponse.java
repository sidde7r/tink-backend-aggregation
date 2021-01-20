package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.entities.TppMessagesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericResponse {
    private List<TppMessagesEntity> tppMessages;

    @JsonIgnore
    public boolean isKycError() {
        return containsError(ErrorCodes.KYC_INVALID);
    }

    @JsonIgnore
    public boolean isMissingBankAgreement() {
        return containsError(ErrorCodes.MISSING_BANK_AGREEMENT);
    }

    public boolean isNoProfileAvailable() {
        return containsError(ErrorCodes.NO_PROFILE_AVAILABLE);
    }

    @JsonIgnore
    public boolean requiresSca() {
        return containsError(ErrorCodes.SCA_REQUIRED);
    }

    @JsonIgnore
    public boolean refreshTokenHasExpired() {
        return containsError(ErrorCodes.REFRESH_TOKEN_EXPIRED);
    }

    @JsonIgnore
    public boolean hasWrongUserId() {
        return containsError(ErrorCodes.WRONG_USER_ID);
    }

    @JsonIgnore
    public boolean isLoginInterrupted() {
        return containsError(ErrorCodes.LOGIN_SESSION_INTERRUPTED);
    }

    @JsonIgnore
    public boolean hasEmptyUserId() {
        return containsError(ErrorCodes.EMPTY_USER_ID);
    }

    @JsonIgnore
    public boolean isConsentInvalid() {
        return containsError(ErrorCodes.CONSENT_INVALID)
                || containsError(ErrorCodes.CONSENT_UNKNOWN);
    }

    @JsonIgnore
    public boolean isConsentExpired() {
        return containsError(ErrorCodes.CONSENT_EXPIRED);
    }

    @JsonIgnore
    public boolean isResourceUnknown() {
        return containsError(ErrorCodes.RESOURCE_UNKNOWN);
    }

    @JsonIgnore
    public boolean hasAuthenticationExpired() {
        return containsError(ErrorCodes.AUTHORIZATION_EXPIRED);
    }

    @JsonIgnore
    public boolean isBadRequest() {
        return containsError(ErrorCodes.FORMAT_ERROR);
    }

    @JsonIgnore
    public boolean hasInsufficientFunds() {
        return containsError(ErrorCodes.INSUFFICIENT_FUNDS);
    }

    @JsonIgnore
    public boolean isInvalidRecipient() {
        return containsError(ErrorCodes.INVALID_RECIPIENT);
    }

    @JsonIgnore
    public boolean isAgreementMissing() {
        return containsError(ErrorCodes.MISSING_CT_AGREEMENT);
    }

    @JsonIgnore
    private boolean containsError(String errorCode) {
        return ListUtils.emptyIfNull(tppMessages).stream()
                .anyMatch(
                        tppMessage ->
                                errorCode.equalsIgnoreCase(tppMessage.getCode())
                                        || errorCode.equalsIgnoreCase(tppMessage.getText()));
    }

    @JsonIgnore
    public String getErrorMessage(String errorCode) {
        return tppMessages.stream()
                .filter(tppMessage -> errorCode.equalsIgnoreCase(tppMessage.getCode()))
                .findFirst()
                .get()
                .getText();
    }

    public String getErrorText() {
        return tppMessages.stream()
                .map(TppMessagesEntity::getText)
                .findFirst()
                .orElse(EndUserMessage.UNKNOWN_ERROR.getKey().get());
    }
}
