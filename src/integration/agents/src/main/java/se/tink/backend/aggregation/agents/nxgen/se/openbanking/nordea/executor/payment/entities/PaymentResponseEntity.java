package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.enums.NordeaPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

import java.util.List;

@JsonObject
public class PaymentResponseEntity {
    @JsonProperty("_id")
    private String id;

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

    @Override
    public String toString() {
        return "Payment{"
                + "paymentStatus='"
                + paymentStatus
                + '\''
                + ", links="
                + links
                + ", amount="
                + amount
                + ", currency='"
                + currency
                + '\''
                + ", creditor="
                + creditor
                + ", debtor="
                + debtor
                + '}';
    }

    public PaymentResponse toTinkPaymentResponse(PaymentType paymentType) {
        Payment tinkPayment =
                new Payment(
                        creditor.toTinkCreditor(),
                        debtor.toTinkDebtor(),
                        Amount.valueOf(currency, Double.valueOf(amount * 100).longValue(), 2),
                        null,
                        currency,
                        paymentType);
        tinkPayment.setProviderId(id);
        tinkPayment.setStatus(
                NordeaPaymentStatus.mapToTinkPaymentStatus(
                        NordeaPaymentStatus.fromString(paymentStatus)));
        return new PaymentResponse(tinkPayment);
    }
}
