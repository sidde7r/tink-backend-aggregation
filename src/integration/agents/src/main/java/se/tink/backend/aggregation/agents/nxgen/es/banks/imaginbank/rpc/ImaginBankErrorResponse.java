package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.ErrorCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ImaginBankErrorResponse {
    @JsonProperty("codigo")
    private String code;

    @JsonProperty("mensaje")
    private String message;

    @JsonIgnore
    public boolean isAccountBlocked() {
        return ImaginBankConstants.ErrorCode.ACCOUNT_BLOCKED.contains(
                Strings.nullToEmpty(code).trim());
    }

    @JsonIgnore
    public boolean isAccessBanned() {
        return ErrorCode.ACCESS_BANNED.contains(Strings.nullToEmpty(code).trim());
    }

    @JsonIgnore
    public boolean isIdentificationIncorrect() {
        return ImaginBankConstants.ErrorCode.INCORRECT_CREDENTIALS.contains(
                Strings.nullToEmpty(code).trim());
    }

    @JsonIgnore
    public boolean isCurrentlyUnavailable() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(ImaginBankConstants.ErrorCode.UNAVAILABLE);
    }

    @JsonIgnore
    public boolean isTemporaryProblem() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(ImaginBankConstants.ErrorCode.TEMPORARY_PROBLEM);
    }
}
