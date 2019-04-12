package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class ResponseEntity {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("entry_date_time")
    private String entryDateTime;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("_links")
    private List<LinksEntity> links;

    private String amount;
    private String currency;
    private DebtorEntity debtor;
    private CreditorEntity creditor;

    public String getId() {
        return id;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
