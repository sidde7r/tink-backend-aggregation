package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class PaymentResponseEntity {
    @JsonProperty("_id")
    private String todoWhichId;
    @JsonProperty("payment_status")
    private String paymentStatus;
    @JsonProperty("_links")
    private List<LinkEntity> links;

    private double amount;
    private String currency;

    private CreditorEntity creditor;
    private DebtorEntity debtor;

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
