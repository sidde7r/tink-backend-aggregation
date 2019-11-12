package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PutConsentsRequest {

    private List<AccountIdEntity> balances;
    private List<AccountIdEntity> transactions;
    private Boolean trustedBeneficiaries;
    private Boolean psuIdentity;

    public PutConsentsRequest(
            final List<AccountIdEntity> balances,
            final List<AccountIdEntity> transactions,
            final Boolean trustedBeneficiaries,
            final Boolean psuIdentity) {
        this.balances = balances;
        this.transactions = transactions;
        this.trustedBeneficiaries = trustedBeneficiaries;
        this.psuIdentity = psuIdentity;
    }
}
