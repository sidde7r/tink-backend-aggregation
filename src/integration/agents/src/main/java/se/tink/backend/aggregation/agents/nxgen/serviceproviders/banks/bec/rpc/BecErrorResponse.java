package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessageLocalizedParts;
import se.tink.backend.aggregation.annotations.JsonObject;
import src.integration.nemid.NemIdSupportedLanguageCode;

@JsonObject
public class BecErrorResponse {
    private String action;
    private String message;

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return Strings.nullToEmpty(message);
    }

    public boolean isWithoutMortgage() {
        return doesMessageContainAnyLocalizedMessagePart(ErrorMessageLocalizedParts.NO_MORTGAGE);
    }

    public boolean noDetailsExist() {
        return doesMessageContainAnyLocalizedMessagePart(
                ErrorMessageLocalizedParts.LOAN_NO_DETAILS_EXIST);
    }

    public boolean functionIsNotAvailable() {
        return doesMessageContainAnyLocalizedMessagePart(
                ErrorMessageLocalizedParts.FUNCTION_NOT_AVAILABLE);
    }

    private boolean doesMessageContainAnyLocalizedMessagePart(
            Map<NemIdSupportedLanguageCode, String> localizedErrorParts) {
        return localizedErrorParts.values().stream()
                .anyMatch(
                        errorPart -> getMessage().toLowerCase().contains(errorPart.toLowerCase()));
    }
}
