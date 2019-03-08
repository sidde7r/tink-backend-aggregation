package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import static com.google.common.base.Predicates.not;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class InternalBankTransferRequest {
    @JsonProperty private int amount;
    @JsonProperty private String speed;
    @JsonProperty private String from;
    @JsonProperty private String to;

    @JsonProperty("own_message")
    private String message;

    @JsonProperty("to_account_number_type")
    private String toAccountNumberType;

    @JsonProperty private String type;
    @JsonProperty private String currency;
    @JsonProperty private String due;

    public InternalBankTransferRequest() {
        this.speed = NordeaSEConstants.Transfer.SPEED;
        // To account type field is mandatory but it seems like anything can be set to that field
        this.toAccountNumberType = NordeaSEConstants.Transfer.TO_ACCOUNT_TYPE;
        this.type = NordeaSEConstants.Transfer.OWN_TRANSFER;
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
    public void setTo(AccountEntity destinationAccount) {
        this.to = destinationAccount.getUnformattedAccountNumber();
    }

    @JsonIgnore
    public void setMessage(Transfer transfer, TransferMessageFormatter transferMessageFormatter) {
        this.message = getInternalTransferMessage(transfer, transferMessageFormatter);
    }

    @JsonIgnore
    public void setDue(Transfer transfer) {
        this.due = findOrCreateDueDateFor(transfer.getDueDate());
    }

    /**
     * Since Nordea only has one message field, we want to have their default formatting of transfer
     * messages when transferring between two Nordea accounts. How they set the default message is
     * better, since they internally have different values on the source and destination account,
     * but for us using the API we can only set one message. So default to empty string to let them
     * decide message if our client hasn't set any message.
     *
     * <p>If the client has set the destination message we use it with the formatter (in order to
     * get it cut off if it's too long).
     */
    @JsonIgnore
    private String getInternalTransferMessage(
            Transfer transfer, TransferMessageFormatter transferMessageFormatter) {
        return Optional.ofNullable(transfer)
                .map(Transfer::getDestinationMessage)
                .filter(not(Strings::isNullOrEmpty))
                .map(s -> transferMessageFormatter.getDestinationMessage(transfer, true))
                .orElse("");
    }
}
