package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonDouble.JsonType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
public class RegisterPaymentRequest {

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
        this.date = date;
        this.type = type;
        this.recipientId = recipientId;
        this.fromAccountId = fromAccountId;
        this.einvoiceReference = eInvoiceReference;
    }

    public static RegisterPaymentRequest createEinvoicePayment(
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String recipientId,
            String fromAccountId,
            String eInvoiceReference) {

        return RegisterPaymentRequest.create(
                amount,
                remittanceInformation,
                date,
                recipientId,
                fromAccountId,
                SwedbankBaseConstants.PaymentType.EINVOICE,
                eInvoiceReference);
    }

    public static RegisterPaymentRequest createPayment(
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String recipientId,
            String fromAccountId) {

        return RegisterPaymentRequest.create(
                amount,
                remittanceInformation,
                date,
                recipientId,
                fromAccountId,
                SwedbankBaseConstants.PaymentType.DOMESTIC,
                null);
    }

    private static RegisterPaymentRequest create(
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String recipientId,
            String fromAccountId,
            String type,
            String eInvoiceReference) {

        return new RegisterPaymentRequest(
                amount,
                ReferenceEntity.create(remittanceInformation),
                date,
                recipientId,
                fromAccountId,
                type,
                eInvoiceReference);
    }
}
