package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenWebApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

public class RaiffeisenTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private static final Logger logger = LoggerFactory.getLogger(RaiffeisenTransactionalAccountFetcher.class);
    private RaiffeisenWebApiClient apiClient;
    private RaiffeisenSessionStorage sessionStorage;

    public RaiffeisenTransactionalAccountFetcher(final RaiffeisenWebApiClient apiClient,
            final RaiffeisenSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    private static Optional<AccountTypes> accountTypeFromString(String accountType) {
        switch (accountType) {
        case "G":
            return Optional.of(AccountTypes.CHECKING);
        case "U":
            return Optional.of(AccountTypes.SAVINGS);
        default:
            // ignore unknown account types
            return Optional.empty();
        }
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountResponse accountResponse = apiClient.getAccountResponse(sessionStorage.getWebLoginResponse()
                .orElseThrow(() -> new IllegalStateException("Could not find login response when fetching accounts")));
        Collection<TransactionalAccount> res = new HashSet<>();
        for (AccountEntity ae : accountResponse.getAccounts()) {
            Optional<AccountTypes> at = accountTypeFromString(ae.getAccountTypeCode());
            if (!at.isPresent()) {
                logger.warn("Unrecognized account type found: \"{}\".", ae.getAccountTypeCode());
                continue;
            }
            TransactionalAccount ta = TransactionalAccount
                    .builder(at.get(), ae.getIban(),
                            new Amount(ae.getBalance().getCurrency(), ae.getBalance().getAmount()))
                    .setName(ae.getIban())
                    .setHolderName(new HolderName(ae.getUsername()))
                    .setAccountNumber(ae.getIban())
                    .build();
            res.add(ta);
        }
        return res;
    }
}

