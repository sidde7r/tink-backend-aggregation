package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsPaymentLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
public class SibsCancelPaymentResponse {

    private SibsTransactionStatus transactionStatus;

    @JsonProperty("_links")
    private SibsPaymentLinkEntity links;

    private String psuMessage;
    private List<SibsTppMessageEntity> tppMessage;

    public SibsTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(SibsTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public SibsPaymentLinkEntity getLinks() {
        return links;
    }

    public void setLinks(SibsPaymentLinkEntity links) {
        this.links = links;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }

    public List<SibsTppMessageEntity> getTppMessage() {
        return tppMessage;
    }

    public void setTppMessage(List<SibsTppMessageEntity> tppMessage) {
        this.tppMessage = tppMessage;
    }

    public PaymentResponse toTinkResponse() {
        Payment.Builder paymentBuilder = new Builder();
        Payment build = paymentBuilder.withStatus(getTransactionStatus().getTinkStatus()).build();
        return new PaymentResponse(build, new TemporaryStorage());
    }
}
