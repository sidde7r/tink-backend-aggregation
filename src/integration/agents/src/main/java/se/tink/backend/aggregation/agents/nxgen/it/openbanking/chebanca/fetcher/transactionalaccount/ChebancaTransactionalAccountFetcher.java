package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.ACCOUNTS_FETCH_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.BALANCES_FETCH_FAILED;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.BalancesDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RequiredArgsConstructor
public class ChebancaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ChebancaApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        HttpResponse httpResponse = apiClient.getAccounts();
        HttpResponseChecker.checkIfSuccessfulResponse(
                httpResponse, HttpServletResponse.SC_OK, ACCOUNTS_FETCH_FAILED);
        GetAccountsResponse getAccountsResponse = httpResponse.getBody(GetAccountsResponse.class);

        return getAccountsResponse.getData().getAccounts().stream()
                .filter(TransactionalAccountMapper::isAccountOfInterest)
                .map(this::toTinkAccount)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {
        HttpResponse response = apiClient.getBalances(accountEntity.getAccountId());
        HttpResponseChecker.checkIfSuccessfulResponse(
                response, HttpServletResponse.SC_OK, BALANCES_FETCH_FAILED);

        BalancesDataEntity balances = response.getBody(GetBalancesResponse.class).getData();
        return TransactionalAccountMapper.mapToTinkAccount(accountEntity, balances);
    }
}
