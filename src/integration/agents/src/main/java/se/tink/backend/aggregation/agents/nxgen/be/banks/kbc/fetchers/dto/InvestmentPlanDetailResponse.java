package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants.Investments.LEFT_TO_INVEST;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class InvestmentPlanDetailResponse extends HeaderResponse {

    private TypeValuePair agreementNumberIban;
    private TypeValuePair beneficiary;
    private List<InvestmentPlanInvestmentDistributionDto> investmentDistributions;
    private List<InvestmentPlanPositionDto> positions;

    public String toIban() {
        return Optional.ofNullable(agreementNumberIban).map(TypeValuePair::getValue).orElse("");
    }

    public List<String> toInvestments() {
        return Optional.ofNullable(investmentDistributions).orElse(Collections.emptyList()).stream()
                .map(InvestmentPlanInvestmentDistributionDto::getName)
                .filter(Objects::nonNull)
                .map(TypeValuePair::getValue)
                .collect(Collectors.toList());
    }

    public ExactCurrencyAmount toCashBalance() {
        if (CollectionUtils.isEmpty(positions)) {
            return ExactCurrencyAmount.zero("EUR");
        }

        Optional<InvestmentPlanPositionDto> investmentPlanOpt =
                positions.stream()
                        .filter(
                                position ->
                                        Optional.ofNullable(position.getProductName())
                                                .map(TypeValuePair::getValue)
                                                .filter(
                                                        value ->
                                                                LEFT_TO_INVEST.equalsIgnoreCase(
                                                                        value))
                                                .isPresent())
                        .filter(
                                position ->
                                        Optional.ofNullable(position.getAmount())
                                                .map(TypeValuePair::getValue)
                                                .isPresent())
                        .filter(
                                position ->
                                        Optional.ofNullable(position.getCurrency())
                                                .map(TypeValuePair::getValue)
                                                .isPresent())
                        .findFirst();

        if (investmentPlanOpt.isPresent()) {
            InvestmentPlanPositionDto investmentPlan = investmentPlanOpt.get();
            return ExactCurrencyAmount.of(
                    Double.parseDouble(investmentPlan.getAmount().getValue()),
                    investmentPlan.getCurrency().getValue());
        } else {
            return ExactCurrencyAmount.zero("EUR");
        }
    }
}
