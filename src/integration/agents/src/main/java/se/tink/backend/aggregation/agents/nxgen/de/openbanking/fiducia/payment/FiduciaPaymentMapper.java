package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.Amt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.CdtTrfTxInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.Cdtr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.CdtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.CreatePaymentXmlRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.CstmrCdtTrfInitn;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.DbtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.GrpHdr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.IbanId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.Id;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.InitgPty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.InstdAmt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.OrgId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.Othr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.PmtId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.PmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.RmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.request.SchmeNm;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.XmlConverter;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class FiduciaPaymentMapper {
    public static final String NEW_LINE = "\r\n";
    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    public String getPaymentRequest(Payment payment) {
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
                        localDateTimeSource.now().format(DateTimeFormatter.ISO_DATE_TIME),
                        String.valueOf(amount.getDoubleValue()),
                        new InitgPty(new Id(new OrgId(other))),
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

        LocalDate executionDate =
                ObjectUtils.firstNonNull(
                        payment.getExecutionDate(),
                        payment.getStartDate(),
                        localDateTimeSource.now().toLocalDate());

        PmtInf paymentInfo =
                new PmtInf(
                        trfInf,
                        debtor == null ? null : new DbtrAcct(new IbanId(debtor.getAccountNumber())),
                        executionDate.format(
                                DateTimeFormatter.ofPattern(
                                        FiduciaConstants.FormValues.DATE_FORMAT)),
                        uuid,
                        String.valueOf(amount.getDoubleValue()));

        return XmlConverter.convertToXml(
                new CreatePaymentXmlRequest(new CstmrCdtTrfInitn(groupHeader, paymentInfo)));
    }

    @SneakyThrows
    public String getRecurringPaymentRequest(Payment payment, String boundary) {
        // This request is a multipart http message, consisting of xml part that is the same as
        // regular one-off payment, and a json part that supplies all fields unique for recurring
        // payments
        String xmlPart = getPaymentRequest(payment);
        String jsonPart = buildRecurringJsonPart(payment);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addTextBody("xml_sct", xmlPart, ContentType.APPLICATION_XML);
        multipartEntityBuilder.addTextBody(
                "json_standingorderType", jsonPart, ContentType.APPLICATION_JSON);
        multipartEntityBuilder.setBoundary(boundary);

        return EntityUtils.toString(multipartEntityBuilder.build());
    }

    private String buildRecurringJsonPart(Payment payment) {
        return SerializationUtils.serializeToString(
                CreateRecurringPaymentRequest.builder()
                        .frequency(payment.getFrequency().toString())
                        .startDate(payment.getStartDate())
                        .endDate(payment.getEndDate())
                        .executionRule(
                                payment.getExecutionRule() != null
                                        ? payment.getExecutionRule().toString()
                                        : null)
                        .dayOfExecution(getDayOfExecution(payment))
                        .build());
    }

    private String getDayOfExecution(Payment payment) {
        switch (payment.getFrequency()) {
            case WEEKLY:
                return String.valueOf(payment.getDayOfWeek().getValue());
            case MONTHLY:
                return payment.getDayOfMonth().toString();
            default:
                throw new IllegalArgumentException(
                        "Frequency is not supported: " + payment.getFrequency());
        }
    }
}
