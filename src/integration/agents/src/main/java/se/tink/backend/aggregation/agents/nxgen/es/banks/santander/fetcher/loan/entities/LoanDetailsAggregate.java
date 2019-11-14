package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import java.time.LocalDate;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoanDetailsAggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoanDetailsAggregate.class);

    private final LoanEntity loanEntity;
    private final LoanDetailsEntity loanDetailsResponse;

    public LoanDetailsAggregate(LoanEntity loanEntity, LoanDetailsEntity loanDetails) {
        this.loanEntity = loanEntity;
        this.loanDetailsResponse = loanDetails;
    }

    public LoanAccount toTinkLoanAccount() {
        final LoanAccount loanAccount =
                LoanAccount.nxBuilder()
                        .withLoanDetails(getLoanDetails())
                        .withId(getLoanId())
                        .build();

        logLoanData(loanAccount, loanEntity, loanDetailsResponse);

        return loanAccount;
    }

    private LoanModule getLoanDetails() {
        return LoanModule.builder()
                .withType(getLoanType())
                .withBalance(loanDetailsResponse.getInitialAmount().getTinkAmount())
                .withInterestRate(Double.valueOf(loanDetailsResponse.getInterest()))
                .setInitialDate(LocalDate.parse(loanDetailsResponse.getStartDate()))
                .setApplicants(Arrays.asList(loanDetailsResponse.getMainHolder()))
                .build();
    }

    private IdModule getLoanId() {
        return IdModule.builder()
                .withUniqueIdentifier(
                        loanEntity.getGeneralInfo().getContractId().getContractNumber())
                .withAccountNumber(loanDetailsResponse.getAssociateAccountNumber())
                .withAccountName(loanEntity.getGeneralInfo().getAlias())
                .addIdentifier(
                        AccountIdentifier.create(
                                Type.TINK,
                                loanEntity.getGeneralInfo().getContractId().getContractNumber()))
                .build();
    }

    private LoanDetails.Type getLoanType() {
        return SantanderEsConstants.LOAN_TYPES.getOrDefault(
                loanEntity.getGeneralInfo().getContractId().getProduct(), LoanDetails.Type.OTHER);
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
