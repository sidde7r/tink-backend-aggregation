package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@AllArgsConstructor
public class PutConsentsRequest {

    private List<AccountIdEntity> balances;
    private List<AccountIdEntity> transactions;
    private Boolean trustedBeneficiaries;
    private Boolean psuIdentity;
}
