package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LaCaixaErrorResponse {
    @JsonProperty("codigo")
    private String code;

    @JsonProperty("mensaje")
    private String message;

    @JsonIgnore
    public boolean isEmptyList() {
        return Strings.nullToEmpty(code)
                .trim()
                .toUpperCase()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.EMPTY_LIST);
    }

    @JsonIgnore
    public boolean isNoSecurities() {
        return Strings.nullToEmpty(code)
                .trim()
                .toUpperCase()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.NO_SECURITIES);
    }

    @JsonIgnore
    public boolean isUserHasNoLoans() {
        return Strings.nullToEmpty(message)
                .trim()
                .equalsIgnoreCase(LaCaixaConstants.ErrorMessage.NO_ASSOCIATED_ACCOUNT);
    }

    @JsonIgnore
    public boolean isUserHasNoOwnCards() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.NO_OWN_CARDS);
    }
}
