package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
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
    public boolean isIdentificationIncorrect() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(ImaginBankConstants.ErrorCode.INCORRECT_CREDENTIALS);
    }

    @JsonIgnore
    public boolean isCurrentlyUnavailable() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(ImaginBankConstants.ErrorCode.UNAVAILABLE);
    }
}
