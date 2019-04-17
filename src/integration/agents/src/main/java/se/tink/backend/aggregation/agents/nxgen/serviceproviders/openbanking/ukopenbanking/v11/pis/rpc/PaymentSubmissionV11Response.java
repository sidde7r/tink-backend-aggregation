package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities.SubmissionDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentSubmissionV11Response {
    @JsonProperty("Data")
    private SubmissionDataEntity data;

    @JsonIgnore
    public Optional<String> getIntentId() {
        if (Objects.isNull(data)) {
            return Optional.empty();
        }

        return Optional.ofNullable(data.getPaymentId());
    }

    @JsonIgnore
    public Optional<UkOpenBankingApiDefinitions.TransactionIndividualStatus1Code> getStatus() {
        if (Objects.isNull(data)) {
            return Optional.empty();
        }

        return Optional.ofNullable(data.getStatus());
    }
}
