package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.DataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentSetupV11Response {
    @JsonProperty("Data")
    private DataEntity data;
    @JsonProperty("Risk")
    private RiskEntity risk;

    private PaymentSetupV11Response(
            @JsonProperty("Data") DataEntity data,
            @JsonProperty("Risk") RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    @JsonIgnore
    public Optional<UkOpenBankingConstants.TransactionIndividualStatus1Code> getStatus() {
        if (Objects.isNull(data)) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.getStatus());
    }

    @JsonIgnore
    public Optional<String> getIntentId() {
        if (Objects.isNull(data)) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.getPaymentId());
    }
}
