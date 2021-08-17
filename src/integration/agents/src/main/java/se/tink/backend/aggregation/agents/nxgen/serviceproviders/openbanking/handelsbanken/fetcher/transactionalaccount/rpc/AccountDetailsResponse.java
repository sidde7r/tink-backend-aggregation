package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountDetailsResponse {
    private List<BalancesItemEntity> balances;
    private String ownerName;
}
