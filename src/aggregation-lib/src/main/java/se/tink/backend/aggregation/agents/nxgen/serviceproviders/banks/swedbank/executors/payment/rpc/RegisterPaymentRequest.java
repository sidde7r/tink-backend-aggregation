package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterPaymentRequest {
    @JsonIgnore
    private static final String EMPTY_STRING = "";

    private final String amount;
    private final ReferenceEntity reference;
    private final String ocrScanned;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private final Date date;
    private final String type;
    private final String recipientId;
    private final String fromAccountId;

    private RegisterPaymentRequest(String amount, ReferenceEntity reference, Date date, String recipientId,
            String fromAccountId) {
        this.amount = amount;
        this.reference = reference;
        this.ocrScanned = "NO";
        this.date = date;
        this.type = "DOMESTIC";
        this.recipientId = recipientId;
        this.fromAccountId = fromAccountId;
    }

    public static RegisterPaymentRequest create(double amount, String message,
            SwedbankTransferHelper.ReferenceType referenceType, Date date, String recipientId, String fromAccountId) {

        return new RegisterPaymentRequest(
                String.valueOf(amount).replace(".", ","),
                ReferenceEntity.create(message, referenceType),
                date, recipientId, fromAccountId);
    }
}
