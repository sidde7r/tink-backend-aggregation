package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SELoanSegment {

    private List<SELoanProperty> properties;
    private String title;

    public boolean loanInformation() {
        return HandelsbankenSEConstants.Loans.LOAN_INFORMATION.equalsIgnoreCase(title);
    }

    public Optional<SELoanProperty> termsOfChange() {
        return findProperty(SELoanProperty::isTermsOfChange);
    }

    public Optional<SELoanProperty> amortization() {
        return findProperty(SELoanProperty::isAmortization);
    }

    public Optional<SELoanProperty> multipleApplicants() {
        return findProperty(SELoanProperty::isMultipleApplicants);
    }

    private Optional<SELoanProperty> findProperty(Predicate<SELoanProperty> by) {
        if (properties == null) {
            return Optional.empty();
        }
        return properties.stream().filter(by).findFirst();
    }
}
