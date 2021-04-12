package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Currency;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.loan.parsers.NumMonthBoundParser;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators.BankIdValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.deserializers.InterestDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

        return LoanAccount.builder(
                        agreementNumber, ExactCurrencyAmount.of(calculateCurrentDebt(), "SEK"))
                .setBankIdentifier(agreementNumber)
                .setAccountNumber(agreementNumber)
                .setName(lender)
                .addIdentifier(new SwedishIdentifier(agreementNumber))
                .setInterestRate(interestRateFormatted)
                .setDetails(
                        LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                                .setCoApplicant(multipleApplicantValue())
                                .setNextDayOfTermsChange(termsOfChangeValue())
                                .setNumMonthsBound(NumMonthBoundParser.parse(fixationdateText))
                                .setMonthlyAmortization(monthlyAmortization())
                                .build())
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder().bankProductName(lender).build();
    }

    private double calculateCurrentDebt() {
        return -currentDebtAmount.asDouble();
    }

    private Date termsOfChangeValue() {
        return findLoanInformationWith(SELoanSegment::termsOfChange)
                .map(SELoanProperty::asDate)
                .orElse(null);
    }

    private ExactCurrencyAmount monthlyAmortization() {
        return findLoanInformationWith(SELoanSegment::amortization)
                .map(SELoanProperty::asAmortizationValue)
                .map(amount -> ExactCurrencyAmount.of(amount, Currency.SEK))
                .orElse(null);
    }

    private boolean multipleApplicantValue() {
        return findLoanInformationWith(SELoanSegment::multipleApplicants)
                .map(SELoanProperty::asMultipleApplicantValue)
                .orElse(false);
    }

    private Optional<SELoanProperty> findLoanInformationWith(
            Function<SELoanSegment, Optional<SELoanProperty>> property) {
        if (segments == null) {
            return Optional.empty();
        }
        return segments.stream()
                .filter(SELoanSegment::loanInformation)
                .findFirst()
                .flatMap(property);
    }
}
