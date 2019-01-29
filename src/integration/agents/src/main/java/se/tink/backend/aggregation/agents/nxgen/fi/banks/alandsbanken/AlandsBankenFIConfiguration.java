package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AlandsBankenFIConfiguration extends CrossKeyConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(AlandsBankenFIConfiguration.class);

    @Override
    public String getBaseUrl() {
        return AlandsBankenFIConstants.Url.BASE;
    }

    @Override
    public LogTag getLoanLogTag() {
        return AlandsBankenFIConstants.Fetcher.LOAN_LOGGING;
    }

    @Override
    public LogTag getInvestmentPortfolioLogTag() {
        return AlandsBankenFIConstants.Fetcher.INVESTMENT_PORTFOLIO_LOGGING;
    }

    @Override
    public LogTag getInvestmentInstrumentLogTag() {
        return AlandsBankenFIConstants.Fetcher.INVESTMENT_INSTRUMENT_LOGGING;
    }

    @Override
    public TransactionalAccount parseTransactionalAccount(CrossKeyAccount account) {
        HolderName accountHolderName = null;
        if (!Strings.isNullOrEmpty(account.getAccountOwnerName())) {
            accountHolderName = new HolderName(account.getAccountOwnerName());
        }

        return TransactionalAccount.builder(account.translateAccountType(), account.getAccountId(),
                new Amount(account.getCurrency(), account.getBalance()))
                .setAccountNumber(account.getBbanFormatted())
                .setName(account.getAccountNickname())
                .addIdentifiers(getIdentifiers(account))
                .setBankIdentifier(account.getAccountId())
                .setHolderName(accountHolderName)
                .build();
    }

    @Override
    public InvestmentAccount parseInvestmentAccount(CrossKeyAccount account, Portfolio portfolio) {
        HolderName accountHolderName = null;
        if (!Strings.isNullOrEmpty(account.getAccountOwnerName())) {
            accountHolderName = new HolderName(account.getAccountOwnerName());
        }

        return InvestmentAccount.builder(account.getAccountId())
                .setCashBalance(new Amount(account.getCurrency(), 0))
                .setAccountNumber(account.getBbanFormatted())
                .setName(account.getAccountNickname())
                .addIdentifiers(getIdentifiers(account))
                .setBankIdentifier(account.getAccountId())
                .setHolderName(accountHolderName)
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    @Override
    public Transaction parseTinkTransaction(CrossKeyTransaction transaction) {
        logIncomingTransaction(transaction);
        return Transaction.builder()
                .setAmount(new Amount(transaction.getCurrency(), transaction.getAmount()))
                .setDescription(createDescription(transaction))
                .setDate(transaction.getDueDate())
                .setExternalId(transaction.getTransactionId())
                .build();
    }

    // logging method, find out if we can find "pending" transactions
    private void logIncomingTransaction(CrossKeyTransaction transaction) {
        try {
            if (transaction.isIncoming()) {
                LOG.info(String.format("%s - %s",
                        AlandsBankenFIConstants.Fetcher.TRANSACTION_LOGGING,
                        SerializationUtils.serializeToString(transaction)));
            }
        } catch (Exception e) {
            LOG.debug("Could not log transaction " + e.getMessage());
        }
    }

    private Collection<AccountIdentifier> getIdentifiers(CrossKeyAccount account) {
        List<AccountIdentifier> identifiers = new ArrayList<>();
        if (hasContent(account.getBic()) && hasContent(account.getAccountNumber())) {
            identifiers.add(new IbanIdentifier(account.getBic(), account.getAccountNumber()));
        }
        if (hasContent(account.getBban())) {
            identifiers.add(new FinnishIdentifier(account.getBban()));
        }

        return identifiers;
    }

    private String createDescription(CrossKeyTransaction transaction) {
        if (!Strings.isNullOrEmpty(transaction.getOwnNote())) {
            return transaction.getOwnNote();
        } else if (!transaction.isIncoming() && !Strings.isNullOrEmpty(transaction.getReceiverName())) {
            return transaction.getReceiverName();
        }

        return transaction.getTextCode();
    }

    @Override
    public LoanAccount parseLoanAccount(CrossKeyAccount account, LoanDetailsEntity loanDetails) {
        LoanDetails details = getLoanDetails(account, loanDetails);

        return LoanAccount.builder(account.getAccountNumber(), new Amount(account.getCurrency(), account.getBalance()))
                .setAccountNumber(account.getBbanFormatted())
                .setName(account.getAccountNickname())
                .setInterestRate(account.getInterestRate())
                .setBankIdentifier(account.getAccountId())
                .setDetails(details)
                .build();
    }

    private LoanDetails getLoanDetails(CrossKeyAccount account, LoanDetailsEntity loanDetails) {
        return LoanDetails.builder(account.getLoanType())
                .setLoanNumber(account.getBban())
                .setInitialBalance(new Amount(account.getCurrency(), loanDetails.getGrantedAmount()))
                .setInitialDate(loanDetails.getOpeningDate())
                .setNextDayOfTermsChange(loanDetails.getNextInterestAdjustmentDate())
                .build();
    }
}
