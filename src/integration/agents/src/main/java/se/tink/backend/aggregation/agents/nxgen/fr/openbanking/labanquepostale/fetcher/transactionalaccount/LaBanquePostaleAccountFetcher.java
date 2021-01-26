package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.CashAccountType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
public class LaBanquePostaleAccountFetcher extends BerlinGroupAccountFetcher {

    private final LaBanquePostaleAccountConverter accountConverter;

    public LaBanquePostaleAccountFetcher(
            LaBanquePostaleApiClient apiClient, LaBanquePostaleAccountConverter accountConverter) {
        super(apiClient);

        this.accountConverter = accountConverter;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountResponse accountResponse = ((LaBanquePostaleApiClient) apiClient).fetchAccounts();
        if (accountResponse.getAccounts().stream()
                .anyMatch(acc -> CashAccountType.CARD == acc.getCashAccountType())) {
            log.info("Account type different than CACC.");
        }
        return accountConverter.toTinkAccounts(accountResponse);
    }
}
