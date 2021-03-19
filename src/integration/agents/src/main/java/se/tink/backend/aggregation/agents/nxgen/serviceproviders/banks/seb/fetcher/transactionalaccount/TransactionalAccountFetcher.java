package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SebApiClient apiClient;
    private final SebSessionStorage sebSessionStorage;
    private final SebBaseConfiguration sebBaseConfiguration;

    public TransactionalAccountFetcher(
            final SebApiClient apiClient,
            final SebSessionStorage sessionStorage,
            final SebBaseConfiguration sebConfiguration) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.sebSessionStorage = Objects.requireNonNull(sessionStorage);
        this.sebBaseConfiguration = sebConfiguration;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final boolean isBusinessAccount = sebBaseConfiguration.isBusinessAgent();
        final Response response =
                apiClient.fetchAccounts(
                        sebSessionStorage.getCustomerNumber(),
                        ServiceInputValues.DEFAULT_ACCOUNT_TYPE);
        final List<AccountEntity> accountEntities =
                sebBaseConfiguration.getAccountEntities(response).orElseGet(ArrayList::new);
        if (isBusinessAccount) {
            sebSessionStorage.putAccountHolderNameBusiness(response.getHolderNameBusiness());
        }
        AccountTypeMapper mapper =
                sebBaseConfiguration.isBusinessAgent()
                        ? SebConstants.BUSINESS_ACCOUNT_TYPE_MAPPER
                        : SebConstants.ACCOUNT_TYPE_MAPPER;
        return accountEntities.stream()
                .filter(e -> e.isTransactionalAccount(mapper))
                .map(account -> account.toTinkAccount(mapper, sebSessionStorage, isBusinessAccount))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
