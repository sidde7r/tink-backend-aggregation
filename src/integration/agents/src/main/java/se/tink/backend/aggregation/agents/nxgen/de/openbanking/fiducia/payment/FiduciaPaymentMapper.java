package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Amt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.CdtTrfTxInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Cdtr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.CdtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.CstmrCdtTrfInitn;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.DbtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.GrpHdr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.IbanId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Id;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.InitgPty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.InstdAmt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.OrgId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Othr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.PmtId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.PmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.RmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.SchmeNm;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.rpc.CreatePaymentXmlRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@RequiredArgsConstructor
public class FiduciaPaymentMapper implements PaymentMapper<CreatePaymentXmlRequest> {

    private final RandomValueGenerator randomValueGenerator;

    @Override
    public CreatePaymentXmlRequest getPaymentRequest(Payment payment) {
        Creditor creditor = payment.getCreditor();
        Debtor debtor = payment.getDebtor();
        ExactCurrencyAmount amount = payment.getExactCurrencyAmount();

        String uuid = randomValueGenerator.generateRandomAlphanumeric(35);
        Othr other =
                new Othr(
                        FiduciaConstants.FormValues.OTHER_ID,
                        new SchmeNm(FiduciaConstants.FormValues.SCHEME_NAME));

        GrpHdr groupHeader =
                new GrpHdr(
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        String.valueOf(amount.getDoubleValue()),
                        new InitgPty(
                                new Id(new OrgId(other)),
                                FiduciaConstants.FormValues.PAYMENT_INITIATOR),
                        uuid);

        CdtTrfTxInf trfInf =
                new CdtTrfTxInf(
                        new Cdtr(creditor.getName()),
                        new CdtrAcct(new IbanId(creditor.getAccountNumber())),
                        new PmtId(uuid),
                        new Amt(
                                new InstdAmt(
                                        amount.getCurrencyCode(),
                                        String.valueOf(amount.getDoubleValue()))),
                        new RmtInf(payment.getRemittanceInformation().getValue()));

        PmtInf paymentInfo =
                new PmtInf(
                        trfInf,
                        debtor == null ? null : new DbtrAcct(new IbanId(debtor.getAccountNumber())),
                        payment.getExecutionDate()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                FiduciaConstants.FormValues.DATE_FORMAT)),
                        uuid,
                        String.valueOf(amount.getDoubleValue()));

        return new CreatePaymentXmlRequest(new CstmrCdtTrfInitn(groupHeader, paymentInfo));
    }

    @Override
    public CreatePaymentXmlRequest getRecurringPaymentRequest(Payment payment) {
        return getPaymentRequest(payment);
    }
}
