package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.core.Amount;
import se.tink.backend.utils.StringUtils;

@JsonObject
public class LoanAccountEntity extends AccountEntity {

    public Optional<LoanAccount> toLoanAccount(String interest, Date nextDayOfTermsChange) {
        if (fullyFormattedNumber == null || balance == null) {
            return Optional.empty();
        }

        LoanDetails loanDetails = LoanDetails.builder()
                .setLoanNumber(accountNumber)
                .setName(name)
                .setNextDayOfTermsChange(nextDayOfTermsChange)
                .build();

        return Optional.of(
                LoanAccount.builder(fullyFormattedNumber, new Amount(currency, StringUtils.parseAmount(balance)))
                        .setDetails(loanDetails)
                        .setInterestRate(StringUtils.parseAmount(interest))
                        .setName(name)
                        .build());
    }
}
