package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.CstmrCdtTrfInitn;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.PmtInf;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@XmlRootElement(name = "Document")
public class PaymentDocument {
    @XmlElement(name = "CstmrCdtTrfInitn")
    private CstmrCdtTrfInitn cstmrCdtTrfInitn;

    public PaymentDocument() {}

    public PaymentDocument(CstmrCdtTrfInitn cstmrCdtTrfInitn) {
        this.cstmrCdtTrfInitn = cstmrCdtTrfInitn;
    }

    public PaymentResponse toTinkPayment(String paymentId, PaymentStatus status) {
        PmtInf paymentInfo = cstmrCdtTrfInitn.getPmtInf();
        ExactCurrencyAmount amount = paymentInfo.getCdtTrfTxInf().getAmt().getInstdAmt().toAmount();

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(
                                                paymentInfo
                                                        .getCdtTrfTxInf()
                                                        .getCdtrAcct()
                                                        .getId()
                                                        .getIban())))
                        .withDebtor(
                                new Debtor(
                                        new IbanIdentifier(
                                                paymentInfo.getDbtrAcct().getId().getIban())))
                        .withExactCurrencyAmount(amount)
                        .withCurrency(amount.getCurrencyCode())
                        .withUniqueId(paymentId)
                        .withStatus(status)
                        .withType(PaymentType.SEPA);
        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
