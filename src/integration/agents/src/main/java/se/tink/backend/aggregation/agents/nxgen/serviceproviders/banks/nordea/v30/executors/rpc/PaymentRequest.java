package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import static io.vavr.Predicates.not;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.entities.BeneficiariesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class PaymentRequest {
    @JsonProperty private double amount;
    @JsonProperty private String speed;
    @JsonProperty private String from;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty private String to;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("to_account_number_type")
    private String toAccountNumberType;

    @JsonProperty private String message;
    @JsonProperty private String type;
    @JsonProperty private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "CET")
    @JsonProperty
    private Date due;

    @JsonProperty("from_account_number_type")
    private String fromAccountNumberType;

    @JsonIgnore private String id;

    @JsonProperty("own_message")
    private String ownMessage;

    private String reference;

    public PaymentRequest() {
        this.speed = NordeaBaseConstants.Transfer.SPEED;
        this.currency = NordeaBaseConstants.CURRENCY;
    }

    @JsonIgnore
    public void setAmount(Amount amount) {
        this.amount = amount.getValue();
        this.currency = amount.getCurrency();
    }

    @JsonIgnore
    public void setFrom(AccountEntity sourceAccount) {
        this.from = sourceAccount.formatAccountNumber();
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
        this.recipientName = destinationAccount.getName();
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
                .map(t -> t.getRemittanceInformation().getValue())
                .filter(not(Strings::isNullOrEmpty))
                .map(
                        s ->
                                transferMessageFormatter
                                        .getDestinationMessageFromRemittanceInformation(
                                                transfer, false))
                .orElse("");
    }

    @JsonIgnore
    public void setDue(Date due) {
        this.due = due;
    }

    @JsonIgnore
    public String getType() {
        return type;
    }

    @JsonIgnore
    public void setType(String type) {
        this.type = type;
    }

    public void setFromAccountNumberType(String fromAccountNumberType) {
        this.fromAccountNumberType = fromAccountNumberType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOwnMessage(String ownMessage) {
        this.ownMessage = ownMessage;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonIgnore
    public String getApiIdentifier() {
        return Strings.isNullOrEmpty(id) ? reference : id;
    }
}
