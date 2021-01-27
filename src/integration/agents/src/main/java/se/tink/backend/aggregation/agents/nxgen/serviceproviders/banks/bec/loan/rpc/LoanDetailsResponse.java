package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.DetailsInGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LoanDetailsResponse {
    private final ToExactCurrencyAmountMapper toExactCurrencyAmountMapper =
            new ToExactCurrencyAmountMapper(new Locale("da"), " ");

    private static final List<String> MORTGAGE_NAMES =
            ImmutableList.of("PrioritetsLån", "EjendomsLån", "BoligLån");

    private final NumberOfMonthsBoundCalculator numberOfMonthsBoundCalculator =
            new NumberOfMonthsBoundCalculator();

    private List<MortgageLoanDetailsEntity> mortgageLoanDetails;

    public Double getInterestRate() {

        return findFirstMatchingDetail(
                        details ->
                                Arrays.asList("Rentesats", "Interest rate")
                                        .contains(details.getDetailName()))
                .map(
                        detail ->
                                AgentParsingUtils.parsePercentageFormInterest(
                                        detail.getDetailValue()))
                .orElse(null);
    }

    public ExactCurrencyAmount getInitialBalance() {
        return findFirstMatchingDetail(
                        details ->
                                Arrays.asList("Hovedstol", "Principal")
                                        .contains(details.getDetailName()))
                .map(detail -> toExactCurrencyAmountMapper.parse(detail.getDetailValue()))
                .orElse(null);
    }

    public LoanDetails.Type getType() {
        return findFirstMatchingDetail(details -> details.getDetailName().equals("Låntype"))
                .filter(detail -> isMortgage(detail.getDetailValue()))
                .map(detail -> Type.MORTGAGE)
                .orElse(Type.OTHER);
    }

    private boolean isMortgage(final String value) {
        return MORTGAGE_NAMES.stream().anyMatch(value::contains);
    }

    public Integer getNumOfMonthsBound() {
        return findFirstMatchingDetail(
                        details ->
                                Arrays.asList("Restløbetid", "Maturity")
                                        .contains(details.getDetailName()))
                .map(detail -> numberOfMonthsBoundCalculator.calculate(detail.getDetailValue()))
                .filter(v -> v > 0)
                .orElse(null);
    }

    private Optional<DetailsInGroupEntity> findFirstMatchingDetail(
            Predicate<DetailsInGroupEntity> predicate) {
        return mortgageLoanDetails.stream()
                .flatMap(
                        (Function<MortgageLoanDetailsEntity, Stream<DetailsInGroupEntity>>)
                                mortgageLoanDetailsEntity ->
                                        mortgageLoanDetailsEntity.getDetailsInGroup().stream())
                .filter(predicate)
                .findFirst();
    }
}
