package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
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
    public boolean isNoAccounts() {
        return Strings.nullToEmpty(code)
                .trim()
                .toUpperCase()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.NO_ACCOUNTS);
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
        return Strings.nullToEmpty(code)
                .trim()
                .toUpperCase()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.NO_ASSOCIATED_ACCOUNTS);
    }

    @JsonIgnore
    public boolean isUserHasNoOwnCards() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.NO_OWN_CARDS);
    }

    @JsonIgnore
    public boolean isCurrentlyUnavailable() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.UNAVAILABLE);
    }

    @JsonIgnore
    public boolean isAccountBlocked() {
        return LaCaixaConstants.ErrorCode.ACCOUNT_BLOCKED.contains(
                Strings.nullToEmpty(code).trim());
    }

    @JsonIgnore
    public boolean isIdentificationIncorrect() {
        return Strings.nullToEmpty(code)
                .trim()
                .equalsIgnoreCase(LaCaixaConstants.ErrorCode.INCORRECT_CREDENTIALS);
    }

    @JsonIgnore
    public boolean isTemporaryProblem() {
        return LaCaixaConstants.ErrorCode.TEMPORARY_PROBLEM.contains(
                Strings.nullToEmpty(code).trim());
    }
}
