package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.enums.RedsysTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentResponse {
    @JsonProperty private String transactionStatus;
    @JsonProperty private String paymentId;
    @JsonProperty private AmountEntity transactionFees;
    @JsonProperty private boolean transactionFeeIndicator;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;

    @JsonIgnore
    public String getPaymentId() {
        return paymentId;
    }

    @JsonIgnore
    public AmountEntity getTransactionFees() {
        return transactionFees;
    }

    @JsonIgnore
    public RedsysTransactionStatus getTransactionStatus() {
        return RedsysTransactionStatus.fromString(transactionStatus);
    }

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        if (links == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(links.get(linkName));
    }
}
