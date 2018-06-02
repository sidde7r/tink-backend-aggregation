package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;

@JsonObject
public class TransactionDisposalAccountEntity extends AccountEntity {

    public Optional<TransactionalAccount> toTransactionalAccount() {
        return toTransactionalAccount(AccountTypes.OTHER);
    }
}
