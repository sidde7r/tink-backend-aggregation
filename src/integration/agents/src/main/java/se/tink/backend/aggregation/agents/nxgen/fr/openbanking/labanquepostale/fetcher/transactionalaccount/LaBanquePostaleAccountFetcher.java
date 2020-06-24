package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LaBanquePostaleAccountFetcher extends BerlinGroupAccountFetcher {

    private final LaBanquePostaleAccountConverter accountConverter;

    public LaBanquePostaleAccountFetcher(
            LaBanquePostaleApiClient apiClient, LaBanquePostaleAccountConverter accountConverter) {
        super(apiClient);

        this.accountConverter = accountConverter;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return accountConverter.toTinkAccounts(
                ((LaBanquePostaleApiClient) apiClient).fetchAccounts());
    }
}
