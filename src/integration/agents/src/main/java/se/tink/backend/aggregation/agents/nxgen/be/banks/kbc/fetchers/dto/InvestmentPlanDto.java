package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvestmentPlanDto {

    private TypeValuePair initiatorName;
    private TypeValuePair initiatorClientNumber;
    private TypeEncValueTuple agreementNumber;
    private TypeEncValueTuple agreementNumberIban;
    private TypeValuePair totalAmount;
    private TypeValuePair currency;
    private TypeValuePair showPossessionIndicator;
    private TypeValuePair isRound2Euro;

    public InvestmentPlanDto() {}

    public InvestmentPlanDto(
            TypeEncValueTuple agreementNumberIban, TypeValuePair showPossessionIndicator) {
        this.agreementNumber = agreementNumberIban;
        this.showPossessionIndicator = showPossessionIndicator;
    }

    public TypeValuePair getInitiatorName() {
        return initiatorName;
    }

    public TypeValuePair getInitiatorClientNumber() {
        return initiatorClientNumber;
    }

    public TypeEncValueTuple getAgreementNumber() {
        return agreementNumber;
    }

    public TypeEncValueTuple getAgreementNumberIban() {
        return agreementNumberIban;
    }

    public TypeValuePair getTotalAmount() {
        return totalAmount;
    }

    public TypeValuePair getCurrency() {
        return currency;
    }

    public TypeValuePair getShowPossessionIndicator() {
        return showPossessionIndicator;
    }

    public TypeValuePair getIsRound2Euro() {
        return isRound2Euro;
    }

    public String toNumber() {
        return Optional.ofNullable(agreementNumber).map(TypeEncValueTuple::getValue).orElse("");
    }

    public String toIban() {
        return Optional.ofNullable(agreementNumberIban).map(TypeEncValueTuple::getValue).orElse("");
    }

    public String toName() {
        return Optional.ofNullable(initiatorName).map(TypeValuePair::getValue).orElse("");
    }
}
