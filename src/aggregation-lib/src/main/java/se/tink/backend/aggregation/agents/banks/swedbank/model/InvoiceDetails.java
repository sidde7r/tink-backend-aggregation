package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.text.ParseException;
import java.util.List;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceDetails {

    private TransactionAccountEntity fromAccount;
    private String id;
    private String type;
    private String date;
    private String amount;
    private List<TransactionAccountGroupEntity> fromAccountGroups;
    private TransferTransactionDetails payment;
    private LinksEntity links;

    public TransactionAccountEntity getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(TransactionAccountEntity fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return Strings.isNullOrEmpty(type) ? null : type.toUpperCase();
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<TransactionAccountGroupEntity> getFromAccountGroups() {
        return fromAccountGroups;
    }

    public void setFromAccountGroups(List<TransactionAccountGroupEntity> fromAccountGroups) {
        this.fromAccountGroups = fromAccountGroups;
    }

    public TransferTransactionDetails getPayment() {
        return payment;
    }

    public void setPayment(TransferTransactionDetails payment) {
        this.payment = payment;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public Transfer toTransfer(boolean isToTransferOfConfirmedPayment) {
        Transfer transfer = new Transfer();

        setTransferAmount(transfer, isToTransferOfConfirmedPayment);
        transfer.setDestinationMessage(getPayment().getReference().getValue());

        // We sometimes get � or ï¿½ (Unicode Replacement Character) from the bank and these characters are not allowed
        // when we send it back again, leading to failed transfers. For now, the safest option is to remove them.
        String payeeName = Strings.nullToEmpty(getPayment().getPayee().getName());
        transfer.setSourceMessage(payeeName.replaceAll("\uFFFD", "").replaceAll("ï¿½", ""));

        transfer.setType(getPayment().getTransferType());
        transfer.setDestination(getPayment().getPayee().generalGetAccountIdentifier());
        setTransferDueDate(transfer, isToTransferOfConfirmedPayment);

        return transfer;
    }

    private void setTransferAmount(Transfer transfer, boolean isToTransferOfConfirmedPayment) {
        if (isToTransferOfConfirmedPayment) {
            transfer.setAmount(Amount.inSEK(StringUtils.parseAmount(getAmount())));
        } else {
            transfer.setAmount(Amount.inSEK(StringUtils.parseAmount(getPayment().getAmount())));
        }
    }

    private void setTransferDueDate(Transfer transfer, boolean isToTransferOfConfirmedPayment) {
        try {
            if (isToTransferOfConfirmedPayment) {
                transfer.setDueDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(getDate()));
            } else {
                transfer.setDueDate(ThreadSafeDateFormat.FORMATTER_DAILY.parse(getPayment().getDueDate()));
            }
        } catch (ParseException e) {
            // Don't set any dueDate.
        }
    }

    @Override
    public String toString() {
        return toTransfer(false).toString();
    }
}
