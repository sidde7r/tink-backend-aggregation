package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
@Data
public class AccountResponse implements BerlinGroupAccountResponse {
    protected List<AccountEntity> accounts;

    @Override
    public Collection<TransactionalAccount> toTinkAccounts() {
        /* Logic moved from POJO to LaBanquePostaleAccountConverter. */
        throw new NotImplementedException(
                "fetchAccounts() method is not implemented. Logic moved from POJO to LaBanquePostaleAccountConverter.");
    }
}
