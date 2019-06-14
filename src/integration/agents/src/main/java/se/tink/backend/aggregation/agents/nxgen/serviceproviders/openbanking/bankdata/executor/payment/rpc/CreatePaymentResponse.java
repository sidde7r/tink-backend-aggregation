package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.enums.BankdataPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    private String paymentId;

    @JsonProperty("_links")
    private LinkEntity links;

    private String paymentStatus;

    public PaymentResponse toTinkPayment(
            String debtorAccountNumber, String creditorAccountNumber, PaymentType type) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withCreditor(new Creditor(new DanishIdentifier(creditorAccountNumber)))
                        .withDebtor(new Debtor(new DanishIdentifier(debtorAccountNumber)))
                        .withStatus(
                                BankdataPaymentStatus.fromString(paymentStatus).getPaymentStatus())
                        .withType(type);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public LinkEntity getLinks() {
        return links;
    }

    public String getDetailsLink() {
        return links.getDetailsLink();
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
