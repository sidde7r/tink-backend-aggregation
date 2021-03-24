package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import java.time.LocalDate;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class LoanDetailsAggregate {
    private final LoanEntity loanEntity;
    private final LoanDetailsEntity loanDetailsResponse;

    public LoanDetailsAggregate(LoanEntity loanEntity, LoanDetailsEntity loanDetails) {
        this.loanEntity = loanEntity;
        this.loanDetailsResponse = loanDetails;
    }

    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(getLoanDetails())
                .withId(getLoanId())
                .build();
    }

    private LoanModule getLoanDetails() {
        return LoanModule.builder()
                .withType(getLoanType())
                .withBalance(loanDetailsResponse.getInitialAmount().getTinkAmount())
                .withInterestRate(
                        AgentParsingUtils.parsePercentageFormInterest(
                                loanDetailsResponse.getInterest()))
                .setInitialDate(LocalDate.parse(loanDetailsResponse.getStartDate()))
                .setApplicants(Arrays.asList(loanDetailsResponse.getMainHolder()))
                .build();
    }

    private IdModule getLoanId() {
        return IdModule.builder()
                .withUniqueIdentifier(
                        loanEntity.getGeneralInfo().getContractId().getContractNumber())
                .withAccountNumber(loanEntity.getGeneralInfo().getContractDescription())
                .withAccountName(loanEntity.getGeneralInfo().getAlias())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.TINK,
                                loanEntity.getGeneralInfo().getContractId().getContractNumber()))
                .build();
    }

    private LoanDetails.Type getLoanType() {
        return SantanderEsConstants.LOAN_TYPES.getOrDefault(
                loanEntity.getGeneralInfo().getContractId().getProduct(), LoanDetails.Type.OTHER);
    }
}
