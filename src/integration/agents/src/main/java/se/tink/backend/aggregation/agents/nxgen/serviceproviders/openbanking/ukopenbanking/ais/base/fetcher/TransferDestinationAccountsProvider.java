package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;

public interface TransferDestinationAccountsProvider {

    List<? extends GeneralAccountEntity> getTrustedBeneficiariesAccounts(Account account);
}
