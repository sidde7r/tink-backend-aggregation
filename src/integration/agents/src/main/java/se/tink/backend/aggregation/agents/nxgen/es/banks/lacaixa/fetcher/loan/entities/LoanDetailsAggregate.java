package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.strings.StringUtils;

public class LoanDetailsAggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoanDetailsAggregate.class);

    private final LoanEntity loanEntity;
    private final LoanDetailsResponse loanDetailsResponse;

    public LoanDetailsAggregate(LoanEntity loanEntity, LoanDetailsResponse loanDetails) {
        this.loanEntity = loanEntity;
        this.loanDetailsResponse = loanDetails;
    }

    public LoanAccount toTinkLoanAccount() {
        LoanAccount loanAccount =
                LoanAccount.nxBuilder()
                        .withLoanDetails(getLoanModule())
                        .withId(getIdModule())
                        .addHolderName(loanDetailsResponse.getTitle())
                        .build();

        logUnknownLoanAccounts(loanAccount, loanEntity, loanDetailsResponse);

        return loanAccount;
    }

    private LoanModule getLoanModule() {
        return LoanModule.builder()
                .withType(getLoanType())
                .withBalance(getBalance())
                .withInterestRate(getInterestRate())
                .setInitialBalance(getInitialBalance())
                .setApplicants(Collections.singletonList(loanDetailsResponse.getTitle()))
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(loanEntity.getContractNumber())
                .withAccountNumber(loanEntity.getContractNumber())
                .withAccountName(loanEntity.getContractDescription())
                .addIdentifier(new IbanIdentifier(loanDetailsResponse.getRelatedAccountNumber()))
                .setProductName(loanEntity.getContractDescription())
                .build();
    }

    private LoanDetails.Type getLoanType() {
        return LaCaixaConstants.LOAN_TYPE_MAPPER
                .translate(loanEntity.getProductCode())
                .orElse(LoanDetails.Type.OTHER);
    }

    private ExactCurrencyAmount getBalance() {
        return new ExactCurrencyAmount(
                        BigDecimal.valueOf(StringUtils.parseAmount(loanEntity.getAmountToPay())),
                        loanEntity.getCurrencyToPay())
                .negate();
    }

    private Double getInterestRate() {
        LOGGER.info("Interest rate value is {}", this.loanDetailsResponse.getNominalInterest());
        return Optional.ofNullable(loanDetailsResponse)
                .map(LoanDetailsResponse::getNominalInterest)
                .map(AgentParsingUtils::parsePercentageFormInterest)
                .orElse(0.0);
    }

    private ExactCurrencyAmount getInitialBalance() {
        return new ExactCurrencyAmount(
                        BigDecimal.valueOf(StringUtils.parseAmount(loanEntity.getInitialBalance())),
                        loanEntity.getCurrency())
                .negate();
    }

    // logging method to discover different types of loans than mortgage
    private void logUnknownLoanAccounts(
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
