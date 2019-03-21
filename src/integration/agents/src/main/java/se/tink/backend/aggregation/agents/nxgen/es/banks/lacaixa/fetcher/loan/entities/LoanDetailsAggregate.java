package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.LaCaixaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

public class LoanDetailsAggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(LaCaixaLoanFetcher.class);

    private final LoanEntity loanEntity;
    private final LoanDetailsResponse loanDetailsResponse;

    public LoanDetailsAggregate(LoanEntity loanEntity, LoanDetailsResponse loanDetails) {
        this.loanEntity = loanEntity;
        this.loanDetailsResponse = loanDetails;
    }

    public LoanAccount toTinkLoanAccount() {
        final Amount amount =
                new Amount(
                        loanEntity.getCurrency(),
                        StringUtils.parseAmount(loanEntity.getTotalAmount()));

        final Amount amountToPay =
                new Amount(
                                loanEntity.getCurrencyToPay(),
                                StringUtils.parseAmount(loanEntity.getAmountToPay()))
                        .negate();

        final Double interestRate =
                StringUtils.parseAmount(this.loanDetailsResponse.getNominalInterest());

        final LoanDetails loanDetails =
                LoanDetails.builder(getLoanType())
                        .setInitialBalance(amount)
                        .setApplicants(Arrays.asList(loanDetailsResponse.getTitle()))
                        .build();

        final LoanAccount loanAccount =
                LoanAccount.builder(loanEntity.getContractNumber())
                        .setInterestRate(interestRate)
                        .setAccountNumber(loanDetailsResponse.getRelatedAccountNumber())
                        .setBalance(amountToPay)
                        .setName(loanEntity.getContractDescription())
                        .setDetails(loanDetails)
                        .build();

        logLoanData(loanAccount, loanEntity, loanDetailsResponse);

        return loanAccount;
    }

    private LoanDetails.Type getLoanType() {
        return LaCaixaConstants.LOAN_TYPE_MAPPER
                .translate(loanEntity.getProductCode())
                .orElse(LoanDetails.Type.OTHER);
    }

    // logging method to discover different types of loans than mortgage
    private void logLoanData(
            LoanAccount loanAccount,
            LoanEntity loanEntity,
            LoanDetailsResponse loanDetailsResponse) {
        if (LoanDetails.Type.OTHER == loanAccount.getDetails().getType()) {
            LOGGER.info(
                    "Unknown loan type: {} {}",
                    SerializationUtils.serializeToString(loanEntity),
                    SerializationUtils.serializeToString(loanDetailsResponse));
        }
    }
}
