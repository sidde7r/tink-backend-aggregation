package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.parsers.NumMonthBoundParser;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.InterestDeserializer;

import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class HandelsbankenSELoan {

    private String agreementNumber;
    private String lender;
    @JsonDeserialize(using = InterestDeserializer.class)
    private Double interestRateFormatted;
    private HandelsbankenAmount currentDebtAmount;
    private List<SELoanSegment> segments;
    private String fixationdateText;

    public LoanAccount toAccount() {
        BankIdValidator.validate(agreementNumber);

        return LoanAccount.builder(agreementNumber, Amount.inSEK(calculateCurrentDebt()))
                .setBankIdentifier(agreementNumber)
                .setAccountNumber(agreementNumber)
                .setName(lender)
                .addIdentifier(new SwedishIdentifier(agreementNumber))
                .setInterestRate(interestRateFormatted)
                .setDetails(LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                        .setCoApplicant(multipleApplicantValue())
                        .setNextDayOfTermsChange(termsOfChangeValue())
                        .setNumMonthsBound(NumMonthBoundParser.parse(fixationdateText))
                        .setMonthlyAmortization(monthlyAmortization())
                        .build())
                .build();
    }

    private double calculateCurrentDebt() {
        return -currentDebtAmount.asDouble();
    }

    private Date termsOfChangeValue() {
        return findLoanInformationWith(SELoanSegment::termsOfChange)
                .map(SELoanProperty::asDate)
                .orElse(null);
    }

    private Amount monthlyAmortization() {
        return findLoanInformationWith(SELoanSegment::amortization)
                .map(SELoanProperty::asAmortizationValue)
                .map(Amount::inSEK)
                .orElse(null);
    }

    private boolean multipleApplicantValue() {
        return findLoanInformationWith(SELoanSegment::multipleApplicants)
                .map(SELoanProperty::asMultipleApplicantValue)
                .orElse(false);
    }

    private Optional<SELoanProperty> findLoanInformationWith(Function<SELoanSegment, Optional<SELoanProperty>> property) {
        if (segments == null) {
            return Optional.empty();
        }
        return segments.stream().filter(SELoanSegment::loanInformation).findFirst().flatMap(property);
    }

}
