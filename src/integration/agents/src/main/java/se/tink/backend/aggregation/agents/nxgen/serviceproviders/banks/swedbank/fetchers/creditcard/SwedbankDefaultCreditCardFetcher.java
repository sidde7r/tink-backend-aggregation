package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.rpc.DetailedCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class SwedbankDefaultCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, LinkEntity> {
    private final SwedbankDefaultApiClient apiClient;
    private final String defaultCurrency;

    public SwedbankDefaultCreditCardFetcher(
            SwedbankDefaultApiClient apiClient, String defaultCurrency) {
        this.apiClient = Preconditions.checkNotNull(apiClient, "ApiClient cannot be null.");
        ;
        this.defaultCurrency =
                Preconditions.checkNotNull(defaultCurrency, "Default currency cannot be null.");
        ;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> tinkCardAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();
            List<CardAccountEntity> cardAccounts = engagementOverviewResponse.getCardAccounts();

            if (cardAccounts != null) {
                tinkCardAccounts.addAll(
                        cardAccounts.stream()
                                .filter(
                                        CardAccountEntity
                                                ::isNotBlocked) // blocked card accounts have no
                                // links
                                .map(CardAccountEntity::getLinks)
                                .map(LinksEntity::getNext)
                                .map(apiClient::cardAccountDetails)
                                .map(
                                        detailedCardAccountResponse ->
                                                detailedCardAccountResponse.toTinkCreditCardAccount(
                                                        bankProfile, defaultCurrency))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList()));
            }
        }

        return tinkCardAccounts;
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            CreditCardAccount account, LinkEntity key) {
        BankProfile bankProfile =
                account.getFromTemporaryStorage(
                                SwedbankBaseConstants.StorageKey.PROFILE, BankProfile.class)
                        .orElseThrow(() -> new IllegalStateException("No bank profile specified"));
        apiClient.selectProfile(bankProfile);

        if (key != null) {
            return apiClient
                    .cardAccountDetails(key)
                    .toTransactionKeyPaginatorResponse(account, defaultCurrency);
        }

        DetailedCardAccountResponse creditCardResponse =
                account.getFromTemporaryStorage(
                                SwedbankBaseConstants.StorageKey.CREDIT_CARD_RESPONSE,
                                DetailedCardAccountResponse.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No credit card response available"));

        List<CreditCardTransaction> transactions = new ArrayList<>();
        transactions.addAll(creditCardResponse.toTransactions(account, defaultCurrency));

        // Only add reserved transactions once
        transactions.addAll(
                creditCardResponse.reservedTransactionsToTransactions(account, defaultCurrency));

        TransactionKeyPaginatorResponseImpl<LinkEntity> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();

        paginatorResponse.setTransactions(transactions);
        paginatorResponse.setNext(creditCardResponse.getNext());
        return paginatorResponse;
    }
}
