package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbExceptionsHelper;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class DnbAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final DnbApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            AccountListResponse accountListResponse = apiClient.fetchAccounts();
            return accountListResponse.getAccountList().stream()
                    .filter(
                            account ->
                                    !Objects.equals(
                                            account.getProductNumber(),
                                            DnbConstants.ProductNumber.StockAccount))
                    .map(AccountDetailsEntity::toTransactionalAccount)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            if (DnbExceptionsHelper.customerDoesNotHaveAccessToResource(e)) {
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
