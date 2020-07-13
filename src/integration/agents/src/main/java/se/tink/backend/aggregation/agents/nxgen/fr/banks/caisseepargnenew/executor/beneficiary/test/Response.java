package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.executor.beneficiary.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.executor.beneficiary.entity.ErrorsItem;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    @JsonProperty("holderIndicator")
    private boolean holderIndicator;

    @JsonProperty("transferCreditorIdentification")
    private TransferCreditorIdentification transferCreditorIdentification;

    @JsonProperty("creditorType")
    private CreditorType creditorType;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("balance")
    private Balance balance;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("bankLabel")
    private String bankLabel;

    @JsonProperty("designationLabel")
    private String designationLabel;

    @JsonProperty("bic")
    private String bic;

    @JsonProperty("errors")
    private List<ErrorsItem> errors;

    @JsonIgnore private String idTokenHint;

    @JsonIgnore
    public boolean isErrorResponse() {
        return Objects.nonNull(errors) && !errors.isEmpty() && !isBeneficiaryAlreadyExists();
    }

    @JsonIgnore
    private boolean isBeneficiaryAlreadyExists() {
        if (Objects.isNull(errors) || errors.isEmpty()) {
            return false;
        }
        return errors.stream()
                .anyMatch(
                        error ->
                                CaisseEpargneConstants.ErrorCodes.BENEFICIARY.equalsIgnoreCase(
                                                error.getCode())
                                        && ErrorMessages.BENEFICIARY_ALREADY_EXISTS
                                                .equalsIgnoreCase(error.getMessage()));
    }

    @JsonIgnore
    public boolean isNeedExtendedAuthenticationError() {
        return isErrorResponse()
                && errors.stream()
                        .anyMatch(
                                error ->
                                        CaisseEpargneConstants.ErrorCodes
                                                        .INSUFFICIENT_AUTHENTICATION
                                                        .equalsIgnoreCase(error.getCode())
                                                && ErrorMessages.INSUFFICIENT_AUTHENTICATION
                                                        .equalsIgnoreCase(error.getMessage()));
    }

    @JsonIgnore
    public void setIdTokenHint(String idTokenHint) {
        this.idTokenHint = idTokenHint;
    }

    @JsonIgnore
    public String getErrorCode() {
        return errors.stream()
                .filter(item -> !item.getMessage().isEmpty())
                .map(ErrorsItem::getCode)
                .findFirst()
                .orElse("");
    }
}
