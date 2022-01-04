package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountAccessConsentsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountAccessConsentsRequest {
    @JsonProperty("Data")
    private AccountAccessConsentsDataEntity data;

    @JsonProperty("Risk")
    private RiskEntity risk;

    private AccountAccessConsentsRequest(AccountAccessConsentsDataEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    public static AccountAccessConsentsRequest create(Set<String> permissions) {
        return new AccountAccessConsentsRequest(
                AccountAccessConsentsDataEntity.of(permissions), new RiskEntity());
    }
}
