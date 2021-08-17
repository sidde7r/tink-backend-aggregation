package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.fetcher;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HsbcKineticPartyFetcher extends HsbcPartyFetcher {

    private final AccountTypeMapper accountTypeMapper;

    public HsbcKineticPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage,
            AccountTypeMapper accountTypeMapper) {
        super(apiClient, config, persistentStorage);
        this.accountTypeMapper = accountTypeMapper;
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        // HSBC Kinetic doesn't support GET /accounts/{AccountId}/party for Credit Cards
        // https://develop.hsbc.com/sites/default/files/open_banking/HSBC%20Open%20Banking%20TPP%20Implementation%20Guide%20(v3.1).pdf
        if (isCreditCard(account)) {
            return Collections.emptyList();
        }
        return super.fetchAccountParties(account);
    }

    private boolean isCreditCard(AccountEntity account) {
        return accountTypeMapper.getAccountType(account).equals(AccountTypes.CREDIT_CARD);
    }
}
