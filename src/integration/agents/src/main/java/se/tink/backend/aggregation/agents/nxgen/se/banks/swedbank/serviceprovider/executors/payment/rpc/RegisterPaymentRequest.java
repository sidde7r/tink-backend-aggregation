package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonDouble.JsonType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterPaymentRequest {
    @JsonIgnore private static final String EMPTY_STRING = "";

    @JsonDouble(outputType = JsonType.STRING, decimalSeparator = ',')
    private final double amount;

    private final ReferenceEntity reference;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String einvoiceReference;

    private final String ocrScanned;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private final Date date;

    private final String recipientId;
    private final String type;
    private final String fromAccountId;

    private RegisterPaymentRequest(
            double amount,
            ReferenceEntity reference,
            Date date,
            String recipientId,
            String fromAccountId,
            String type,
            String eInvoiceReference) {
        this.amount = amount;
        this.reference = reference;
        this.ocrScanned = "NO";
        this.date = getDateOrNullIfDueDateIsToday(date);
        this.type = type;
        this.recipientId = recipientId;
        this.fromAccountId = fromAccountId;
        this.einvoiceReference = eInvoiceReference;
    }

    /**
     * Swedbank reject today's date as a possible date. If the payment is suppose to be executed
     * today the date field needs to be left blank (null).
     *
     * @return Input date if a future date, null if input date is today's date.
     */
    @JsonIgnore
    private Date getDateOrNullIfDueDateIsToday(Date transferDate) {
        LocalDate todayLocalDate = LocalDate.now(ZoneId.of("CET"));

        LocalDate transferLocalDate =
                transferDate.toInstant().atZone(ZoneId.of("CET")).toLocalDate();

        // Use localdate for comparison as we don't care about time
        if (todayLocalDate.equals(transferLocalDate)) {
            return null;
        }

        return transferDate;
    }

    public static RegisterPaymentRequest createEinvoicePayment(
            double amount,
            String message,
            SwedbankBaseConstants.ReferenceType referenceType,
            Date date,
            String recipientId,
            String fromAccountId,
            String eInvoiceReference) {

        return RegisterPaymentRequest.create(
                amount,
                message,
                referenceType,
                date,
                recipientId,
                fromAccountId,
                SwedbankBaseConstants.PaymentType.EINVOICE,
                eInvoiceReference);
    }

    public static RegisterPaymentRequest createPayment(
            double amount,
            String message,
            SwedbankBaseConstants.ReferenceType referenceType,
            Date date,
            String recipientId,
            String fromAccountId) {

        return RegisterPaymentRequest.create(
                amount,
                message,
                referenceType,
                date,
                recipientId,
                fromAccountId,
                SwedbankBaseConstants.PaymentType.DOMESTIC,
                null);
    }

    private static RegisterPaymentRequest create(
            double amount,
            String message,
            SwedbankBaseConstants.ReferenceType referenceType,
            Date date,
            String recipientId,
            String fromAccountId,
            String type,
            String eInvoiceReference) {

        return new RegisterPaymentRequest(
                amount,
                ReferenceEntity.create(message, referenceType),
                date,
                recipientId,
                fromAccountId,
                type,
                eInvoiceReference);
    }
}
