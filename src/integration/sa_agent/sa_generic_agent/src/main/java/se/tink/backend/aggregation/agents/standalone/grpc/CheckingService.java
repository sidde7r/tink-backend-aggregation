package se.tink.backend.aggregation.agents.standalone.grpc;

import io.grpc.ManagedChannel;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountExclusion;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.sa.services.fetch.account.AccountIdentifierType;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsServiceGrpc;
import se.tink.sa.services.fetch.account.TransactionalAccountType;

public class CheckingService {

    private final FetchAccountsServiceGrpc.FetchAccountsServiceBlockingStub
            fetchAccountsServiceBlockingStub;

    public CheckingService(final ManagedChannel channel) {
        fetchAccountsServiceBlockingStub = FetchAccountsServiceGrpc.newBlockingStub(channel);
    }

    public FetchAccountsResponse fetchCheckingAccounts(final String consetnId) {
        FetchAccountsRequest fetchAccountsRequest =
                FetchAccountsRequest.newBuilder().setConsentId(consetnId).build();
        return mapFetchAccountsResponse(
                fetchAccountsServiceBlockingStub.fetchCheckingAccounts(fetchAccountsRequest));
    }

    public FetchTransactionsResponse fetchCheckingTransactions() {
        // TODO
        return null;
    }

    private FetchAccountsResponse mapFetchAccountsResponse(
            final se.tink.sa.services.fetch.account.FetchAccountsResponse fetchAccountsResponse) {
        return new FetchAccountsResponse(mapAccountList(fetchAccountsResponse.getAccountList()));
    }

    private List<Account> mapAccountList(
            final List<se.tink.sa.services.fetch.account.TransactionalAccount> accountList) {
        return Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
                .map(this::mapAccount)
                .collect(Collectors.toList());
    }

    private Account mapAccount(
            final se.tink.sa.services.fetch.account.TransactionalAccount transactionalAccount) {
        final Account account = new Account();
        account.setAccountNumber(transactionalAccount.getAccountNumber());
        account.setAccountExclusion(
                mapAccountExclusion(transactionalAccount.getAccountExclusion()));
        account.setAvailableCredit(transactionalAccount.getAvailableCredit());
        account.setExactAvailableCredit(
                mapExactCurrencyAmount(transactionalAccount.getExactAvailableCredit()));
        account.setBalance(transactionalAccount.getBalance());
        account.setExactBalance(mapExactCurrencyAmount(transactionalAccount.getExactBalance()));
        account.setCurrencyCode(transactionalAccount.getCurrencyCode());
        account.setBankId(transactionalAccount.getBankId());
        account.setCertainDate(mapFromGoogleDate(transactionalAccount.getCertainDate()));
        account.setCredentialsId(transactionalAccount.getCredentialsId());
        account.setExcluded(transactionalAccount.getExcluded());
        account.setFavored(transactionalAccount.getFavored());
        account.setId(transactionalAccount.getId());
        account.setName(transactionalAccount.getName());
        account.setOwnership(transactionalAccount.getOwnership());
        account.setPayload(transactionalAccount.getPayload());
        account.setType(mapAccountTypes(transactionalAccount.getType()));
        account.setUserId(transactionalAccount.getUserId());
        account.setUserModifiedExcluded(transactionalAccount.getUserModifiedExcluded());
        account.setUserModifiedName(transactionalAccount.getUserModifiedName());
        account.setUserModifiedType(transactionalAccount.getUserModifiedType());
        account.setIdentifiers(mapAccountIdentifiers(transactionalAccount.getIdentifiersList()));

        return account;
    }

    private AccountExclusion mapAccountExclusion(
            final se.tink.sa.services.fetch.account.AccountExclusion accountExclusion) {
        return AccountExclusion.values()[accountExclusion.getNumber()];
    }

    private ExactCurrencyAmount mapExactCurrencyAmount(
            final se.tink.sa.services.fetch.account.ExactCurrencyAmount exactCurrencyAmount) {
        return ExactCurrencyAmount.of(
                exactCurrencyAmount.getValue(), exactCurrencyAmount.getCurrencyCode());
    }

    private Date mapFromGoogleDate(final com.google.type.Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, date.getDay());
        return calendar.getTime();
    }

    private AccountTypes mapAccountTypes(final TransactionalAccountType type) {
        return AccountTypes.values()[type.getNumber()];
    }

    private Collection<AccountIdentifier> mapAccountIdentifiers(
            final List<se.tink.sa.services.fetch.account.AccountIdentifier> accountIdentifiers) {
        return Optional.ofNullable(accountIdentifiers).orElse(Collections.emptyList()).stream()
                .map(this::mapAccountIdentifier)
                .collect(Collectors.toList());
    }

    private AccountIdentifier mapAccountIdentifier(
            final se.tink.sa.services.fetch.account.AccountIdentifier accountIdentifier) {
        return AccountIdentifier.create(
                mapAccountIdentifierType(accountIdentifier.getType()),
                accountIdentifier.getId(),
                accountIdentifier.getName());
    }

    private AccountIdentifier.Type mapAccountIdentifierType(
            final AccountIdentifierType accountIdentifierType) {
        return AccountIdentifier.Type.values()[accountIdentifierType.getNumber()];
    }
}
