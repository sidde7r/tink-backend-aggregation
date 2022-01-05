package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountAccessConsentsDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.BaseV31Response;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountAccessConsentsResponse
        extends BaseV31Response<AccountAccessConsentsDataResponseEntity> {

    @JsonProperty("Data")
    public void setData(AccountAccessConsentsDataResponseEntity data) {
        this.data = data;
    }

    public String getConsentId() {
        return data.getAccountConsentId();
    }

    public Instant getCreationDate() {
        return data.getCreationDateTime();
    }
}
