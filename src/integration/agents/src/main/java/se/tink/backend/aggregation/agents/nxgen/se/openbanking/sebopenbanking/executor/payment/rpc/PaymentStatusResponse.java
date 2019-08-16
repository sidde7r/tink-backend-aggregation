package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.SebPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentStatusResponse {
    private String transactionStatus;
    private List<ScaMethodEntity> scaMethods;

    @JsonProperty("_links")
    private LinksEntity links;

    public String getTransactionStatus() {
        return transactionStatus;
    }

    @JsonIgnore
    public boolean hasMethodSelectionEntity() {
        return links != null && links.hasMethodSelectionEntity();
    }

    @JsonIgnore
    public boolean isReadyForSigning() {
        return hasMethodSelectionEntity()
                && transactionStatus.equalsIgnoreCase(SebPaymentStatus.RCVD.getText());
    }
}
