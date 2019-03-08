package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import static com.google.common.base.Predicates.not;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class PaymentRequest {
    @JsonProperty private int amount;
    @JsonProperty private String speed;
    @JsonProperty private String from;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty private String to;

    @JsonProperty("to_account_number_type")
    private String toAccountNumberType;

    @JsonProperty private String message;
    @JsonProperty private String type;
    @JsonProperty private String currency;
    @JsonProperty private String due;

    public PaymentRequest() {
        this.speed = NordeaSEConstants.Transfer.SPEED;
        this.currency = NordeaSEConstants.CURRENCY;
    }

    @JsonIgnore
    public static String findOrCreateDueDateFor(Date dueDate) {
        return Optional.ofNullable(dueDate)
                .map(ThreadSafeDateFormat.FORMATTER_DAILY::format)
                .orElse(ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()));
    }

    @JsonIgnore
    public void setAmount(Transfer transfer) {
        this.amount = transfer.getAmount().getValue().intValue();
    }

    @JsonIgnore
    public void setFrom(AccountEntity sourceAccount) {
        this.from = sourceAccount.getUnformattedAccountNumber();
    }

    @JsonIgnore
    public void setBankName(BeneficiariesEntity destinationAccount) {
        this.bankName = destinationAccount.getBankName();
    }

    @JsonIgnore
    public String getTo() {
        return to;
    }

    @JsonIgnore
    public void setTo(BeneficiariesEntity destinationAccount) {
        this.to = destinationAccount.getAccountNumber();
    }

    // sets message for payment
    @JsonIgnore
    public void setMessage(String message) {
        this.message = message;
    }

    // sets message for bank transfers
    @JsonIgnore
    public void setMessage(Transfer transfer, TransferMessageFormatter transferMessageFormatter) {
        this.message = getExternalTransferMessage(transfer, transferMessageFormatter);
    }

    @JsonIgnore
    public void setToAccountNumberType(String toAccountNumberType) {
        this.toAccountNumberType = toAccountNumberType;
    }

    @JsonIgnore
    private String getExternalTransferMessage(
            Transfer transfer, TransferMessageFormatter transferMessageFormatter) {
        return Optional.ofNullable(transfer)
                .map(Transfer::getDestinationMessage)
                .filter(not(Strings::isNullOrEmpty))
                .map(s -> transferMessageFormatter.getDestinationMessage(transfer, true))
                .orElse("");
    }

    @JsonIgnore
    public void setDue(Transfer transfer) {
        this.due = findOrCreateDueDateFor(transfer.getDueDate());
    }

    @JsonIgnore
    public String getType() {
        return type;
    }

    @JsonIgnore
    public void setType(String type) {
        this.type = type;
    }
}
