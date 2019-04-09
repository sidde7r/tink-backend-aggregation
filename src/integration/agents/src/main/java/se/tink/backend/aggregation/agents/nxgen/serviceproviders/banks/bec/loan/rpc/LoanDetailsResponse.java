package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.DetailsInGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse {
    private List<MortgageLoanDetailsEntity> mortgageLoanDetails;

    public List<MortgageLoanDetailsEntity> getMortgageLoanDetails() {
        return mortgageLoanDetails;
    }

    public Optional<Double> getInterestRate() {
        Optional<String> interestRate =
                mortgageLoanDetails.stream()
                        .map(MortgageLoanDetailsEntity::getDetailsInGroup)
                        .flatMap(List::stream)
                        .filter(
                                details ->
                                        BecConstants.Loan.INTEREST_DETAILS_KEY.equalsIgnoreCase(
                                                details.getDetailName()))
                        .map(DetailsInGroupEntity::getDetailValue)
                        .findFirst();

        if (!interestRate.isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                AgentParsingUtils.parsePercentageFormInterest(interestRate.get()));
    }
}
