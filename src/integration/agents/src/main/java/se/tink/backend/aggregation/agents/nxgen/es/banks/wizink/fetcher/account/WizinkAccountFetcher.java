package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class WizinkAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private WizinkStorage wizinkStorage;

    public WizinkAccountFetcher(WizinkStorage wizinkStorage) {
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return wizinkStorage.getProductsList().stream()
                .map(productEntity -> productEntity.toTinkAccount(wizinkStorage.getXTokenUser()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
