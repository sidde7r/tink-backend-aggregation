package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class TransactionDisposalAccountEntity extends AccountEntity {

    public Optional<TransactionalAccount> toTransactionalAccount(
            BankProfile bankProfile,
            EngagementTransactionsResponse engagementTransactionsResponse) {
        return toTransactionalAccount(
                bankProfile, AccountTypes.OTHER, engagementTransactionsResponse);
    }
}
