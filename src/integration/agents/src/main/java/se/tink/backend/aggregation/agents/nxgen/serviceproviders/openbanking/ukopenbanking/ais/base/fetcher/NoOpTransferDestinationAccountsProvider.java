package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;

public class NoOpTransferDestinationAccountsProvider
        implements TransferDestinationAccountsProvider {
    @Override
    public List<? extends GeneralAccountEntity> getTrustedBeneficiariesAccounts(Account account) {
        return Collections.emptyList();
    }
}
