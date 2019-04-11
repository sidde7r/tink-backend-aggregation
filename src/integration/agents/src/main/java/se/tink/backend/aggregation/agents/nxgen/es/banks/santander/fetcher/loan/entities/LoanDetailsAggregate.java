package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

public class LoanDetailsAggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoanDetailsAggregate.class);

    private final LoanEntity loanEntity;
    private final LoanDetailsEntity loanDetailsResponse;

    public LoanDetailsAggregate(LoanEntity loanEntity, LoanDetailsEntity loanDetails) {
        this.loanEntity = loanEntity;
        this.loanDetailsResponse = loanDetails;
    }

    public LoanAccount toTinkLoanAccount() {

        final LoanDetails loanDetails =
                LoanDetails.builder(getLoanType())
                        .setInitialBalance(loanDetailsResponse.getInitialAmount().getTinkAmount())
                        .setInitialDate(loanDetailsResponse.getStartDate())
                        .setApplicants(Arrays.asList(loanDetailsResponse.getMainHolder()))
                        .build();

        final LoanAccount loanAccount =
                LoanAccount.builder(loanDetailsResponse.getContractEntity().getContractNumber())
                        .setInterestRate(
                                StringUtils.parseAmount(loanDetailsResponse.getInterestl()))
                        .setAccountNumber(loanDetailsResponse.getAssociateAccountNumber())
                        .setBalance(loanEntity.getBalance().getTinkAmount())
                        .setName(loanEntity.getGeneralInfo().getAlias())
                        .setDetails(loanDetails)
                        .build();

        logLoanData(loanAccount, loanEntity, loanDetailsResponse);

        return loanAccount;
    }

    private LoanDetails.Type getLoanType() {
        // Set to other if we do not know the loan type
        return LoanDetails.Type.OTHER;
    }

    // logging method to discover different types of loans than mortgage
    private void logLoanData(
            LoanAccount loanAccount, LoanEntity loanEntity, LoanDetailsEntity loanDetailsResponse) {
        if (LoanDetails.Type.OTHER == loanAccount.getDetails().getType()) {
            LOGGER.info(
                    "Unknown loan type: {} {}",
                    SerializationUtils.serializeToString(loanEntity),
                    SerializationUtils.serializeToString(loanDetailsResponse));
        }
    }
}
