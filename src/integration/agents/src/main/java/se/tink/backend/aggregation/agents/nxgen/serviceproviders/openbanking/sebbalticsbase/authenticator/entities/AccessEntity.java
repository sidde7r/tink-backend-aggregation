package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities;

import java.util.List;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Setter
public class AccessEntity {

    private List<AccountNumberEntity> accounts;
    private List<AccountNumberEntity> balances;
    private List<AccountNumberEntity> transactions;
}
