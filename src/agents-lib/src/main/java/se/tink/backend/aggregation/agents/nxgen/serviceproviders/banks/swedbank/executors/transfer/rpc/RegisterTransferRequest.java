package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterTransferRequest {
    @JsonIgnore
    private static final String EMPTY_DATE = "";

    private final String amount;
    private final String recipientId;
    private final String fromAccountId;
    private final String date;

    public String getAmount() {
        return amount;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public String getDate() {
        return date;
    }

    private RegisterTransferRequest(double amount, String recipientId, String fromAccountId) {
        this.amount = formatAmount(amount);
        this.recipientId = recipientId;
        this.fromAccountId = fromAccountId;
        this.date = EMPTY_DATE;
    }

    public static RegisterTransferRequest create(double amount, String recipientId, String fromAccountId) {
        return new RegisterTransferRequest(amount, recipientId, fromAccountId);
    }

    private static String formatAmount(double amount) {
        BigDecimal bigDecimalAmount = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
        return bigDecimalAmount.toString().replace(".", ","); // Swedbank uses comma-separator
    }
}
