package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerMarketUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.model.NordeaPartnerTransactionalPaginatorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
@Slf4j
public class NordeaPartnerTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final NordeaPartnerApiClient apiClient;
    private final NordeaPartnerAccountMapper accountMapper;
    private final AgentComponentProvider componentProvider;
    private final CredentialsRequest request;
    private final boolean isOnStaging;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        if (isOnStaging) {
            apiClient.fetchAllData(
                    NordeaPartnerMarketUtil.getStartDate(request.getAccounts(), componentProvider));
            return apiClient.getAllData().toTinkTransactionalAccounts(accountMapper);
        }
        return apiClient.fetchAccounts().toTinkTransactionalAccounts(accountMapper);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        if (isOnStaging) {
            AccountListResponse accounts = apiClient.getAllData();

            List<TransactionEntity> transactions =
                    accounts.getAccounts().stream()
                            .filter(
                                    a ->
                                            a.getAccountId()
                                                    .equalsIgnoreCase(account.getApiIdentifier()))
                            .map(AccountEntity::getTransactions)
                            .findFirst()
                            .orElse(Collections.emptyList());

            if (apiClient.isUserPresent()) {
                return new NordeaPartnerTransactionalPaginatorResponse(
                        getTinkTransactions(transactions, apiClient.getMarket()),
                        null,
                        Optional.of(false));
            }
        }

        if (apiClient.isUserPresent()) {
            try {
                final LocalDate dateLimit =
                        NordeaPartnerMarketUtil.getPaginationStartDate(
                                account, request, componentProvider);
                final AccountTransactionsResponse response =
                        apiClient.fetchAccountTransaction(
                                account.getApiIdentifier(), key, dateLimit);

                return new NordeaPartnerTransactionalPaginatorResponse(
                        response.getTinkTransactions(apiClient.getMarket()),
                        response.getContinuationKey(),
                        response.canFetchMore(dateLimit));
            } catch (BankServiceException e) {
                // don't raise non-actionable alert for frequent 502 and 503 errors
                log.warn(
                        "Ignoring bank service error when fetching transactions: "
                                + e.getMessage());
                return NordeaPartnerTransactionalPaginatorResponse.createEmpty();
            }
        }
        return NordeaPartnerTransactionalPaginatorResponse.createEmpty();
    }

    @JsonIgnore
    public Collection<Transaction> getTinkTransactions(
            List<TransactionEntity> transactions, String market) {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(market))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
