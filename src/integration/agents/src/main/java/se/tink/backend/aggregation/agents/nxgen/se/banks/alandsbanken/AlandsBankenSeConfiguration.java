package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbanken;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class AlandsBankenSeConfiguration extends CrossKeyConfiguration {

    @Override
    public String getBaseUrl() {
        return AlandsBankenSeConstants.Url.BASE;
    }

    @Override
    public TransactionalAccount parseTransactionalAccount(CrossKeyAccount account) {
        HolderName accountHolderName = null;
        if (!Strings.isNullOrEmpty(account.getAccountOwnerName())) {
            accountHolderName = new HolderName(account.getAccountOwnerName());
        }

        return TransactionalAccount.builder(
                        account.translateAccountType(),
                        account.getAccountId(),
                        ExactCurrencyAmount.of(account.getBalance(), account.getCurrency()))
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
                .setCashBalance(ExactCurrencyAmount.zero(account.getCurrency()))
                .setAccountNumber(account.getBbanFormatted())
                .setName(account.getAccountNickname())
                .addIdentifiers(getIdentifiers(account))
                .setBankIdentifier(account.getAccountId())
                .setHolderName(accountHolderName)
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    @Override
    public IdentityData parseIdentityData(IdentityDataResponse identityResponse) {
        if (identityResponse.isFailure()) {
            return null;
        }
        return SeIdentityData.of(
                identityResponse.getFirstName(),
                identityResponse.getLastName(),
                identityResponse.getSsn());
    }

    @Override
    public Transaction parseTinkTransaction(CrossKeyTransaction transaction) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(transaction.getAmount(), transaction.getCurrency()))
                .setDescription(createDescription(transaction))
                .setDate(transaction.getDueDate())
                .build();
    }

    private Collection<AccountIdentifier> getIdentifiers(CrossKeyAccount account) {
        List<AccountIdentifier> identifiers = new ArrayList<>();
        if (hasContent(account.getBic()) && hasContent(account.getAccountNumber())) {
            identifiers.add(new IbanIdentifier(account.getBic(), account.getAccountNumber()));
        }
        if (hasContent(account.getBban())) {
            identifiers.add(new SwedishIdentifier(account.getBban()));
        }

        return identifiers;
    }

    private String createDescription(CrossKeyTransaction transaction) {
        if (!Strings.isNullOrEmpty(transaction.getOwnNote())) {
            return transaction.getOwnNote();
        } else if (!transaction.isIncoming()
                && !Strings.isNullOrEmpty(transaction.getReceiverName())) {
            return transaction.getReceiverName();
        }

        return transaction.getTextCode();
    }

    @Override
    public LoanAccount parseLoanAccount(CrossKeyAccount account, LoanDetailsEntity loanDetails) {
        LoanDetails details = getLoanDetails(account, loanDetails);

        return LoanAccount.builder(
                        account.getAccountId(),
                        ExactCurrencyAmount.of(account.getBalance(), account.getCurrency()))
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
                .setInitialBalance(
                        ExactCurrencyAmount.of(
                                loanDetails.getGrantedAmount(), account.getCurrency()))
                .setInitialDate(loanDetails.getOpeningDate())
                .setNextDayOfTermsChange(loanDetails.getNextInterestAdjustmentDate())
                .build();
    }
}
