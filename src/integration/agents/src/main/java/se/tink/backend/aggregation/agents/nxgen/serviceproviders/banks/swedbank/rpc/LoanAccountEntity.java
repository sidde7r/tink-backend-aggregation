package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class LoanAccountEntity extends AccountEntity {

    public Optional<LoanAccount> toLoanAccount(String interest, Date nextDayOfTermsChange) {
        if (fullyFormattedNumber == null || balance == null) {
            return Optional.empty();
        }

        LoanDetails loanDetails = LoanDetails.builder(LoanDetails.Type.DERIVE_FROM_NAME)
                .setLoanNumber(accountNumber)
                .setNextDayOfTermsChange(nextDayOfTermsChange)
                .build();

        return Optional.of(
                LoanAccount.builder(fullyFormattedNumber, new Amount(currency, StringUtils.parseAmount(balance)))
                        .setAccountNumber(fullyFormattedNumber)
                        .setDetails(loanDetails)
                        .setInterestRate(parsePercentageToDouble(interest))
                        .setName(name)
                        .build());
    }

    private double parsePercentageToDouble(String interestString) {
        // Using BigDecimal for the division to not end up with stuff like 0.016200000000000003
        BigDecimal interest = new BigDecimal(StringUtils.parseAmount(interestString));
        return interest.divide(new BigDecimal(100)).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }
}
