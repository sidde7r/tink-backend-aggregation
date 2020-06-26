package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.einvoice.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ReferenceEntity;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RegisterEInvoiceRequest {
    @JsonIgnore private static final String EMPTY_STRING = "";

    private final String amount;
    private final ReferenceEntity reference;
    private final String ocrScanned;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private final Date date;

    private final String type;
    private final String einvoiceReference;
    private final String recipientId;
    private final String fromAccountId;

    private RegisterEInvoiceRequest(
            String amount,
            ReferenceEntity reference,
            Date date,
            String einvoiceReference,
            String recipientId,
            String fromAccountId) {
        this.amount = amount;
        this.reference = reference;
        this.ocrScanned = "NO";
        this.date = date;
        this.type = "EINVOICE";
        this.einvoiceReference = einvoiceReference;
        this.recipientId = recipientId;
        this.fromAccountId = fromAccountId;
    }

    public static RegisterEInvoiceRequest create(
            double amount,
            RemittanceInformation remittanceInformation,
            Date date,
            String einvoiceReference,
            String recipientId,
            String fromAccountId) {

        return new RegisterEInvoiceRequest(
                String.valueOf(amount).replace(".", ","),
                ReferenceEntity.create(remittanceInformation),
                date,
                einvoiceReference,
                recipientId,
                fromAccountId);
    }
}
