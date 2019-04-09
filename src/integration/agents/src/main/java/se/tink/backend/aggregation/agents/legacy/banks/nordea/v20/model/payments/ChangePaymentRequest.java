package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaAgentUtils;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;
import se.tink.libraries.amount.Amount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePaymentRequest {
    private ChangePaymentIn changePaymentIn = new ChangePaymentIn();

    public ChangePaymentIn getChangePaymentIn() {
        return changePaymentIn;
    }

    public void setChangePaymentIn(ChangePaymentIn changePaymentIn) {
        this.changePaymentIn = changePaymentIn;
    }

    public static ChangePaymentRequest copyFromPaymentDetails(
            List<ProductEntity> sourceAccounts, PaymentDetailsResponseOut details) {
        // From account id on details is not exactly same as on the change object, it's an
        // identifier on the product entity
        Preconditions.checkNotNull(
                details.getFromAccountId(),
                "No from account is set, didn't know this could happen");
        Preconditions.checkNotNull(sourceAccounts, "No source accounts");

        Optional<ProductEntity> fromAccount =
                sourceAccounts.stream()
                        .filter(
                                a ->
                                        NordeaAgentUtils.getAccountIdFilter(
                                                        ImmutableSet.of(details.getFromAccountId()))
                                                .apply(a))
                        .findFirst();
        Preconditions.checkNotNull(fromAccount.orElse(null));

        ChangePaymentRequest changePaymentRequest = new ChangePaymentRequest();

        changePaymentRequest.changePaymentIn.setAmount(details.getAmount());
        changePaymentRequest.changePaymentIn.setBeneficiaryName(details.getBeneficiaryName());
        changePaymentRequest.changePaymentIn.setBeneficiaryNickName(details.getBeneficiaryName());
        changePaymentRequest.changePaymentIn.setCurrency(details.getCurrency());
        changePaymentRequest.changePaymentIn.setDueDate(details.getDueDate());
        changePaymentRequest.changePaymentIn.setMessageRow(details.getMessageRow());
        changePaymentRequest.changePaymentIn.setPaymentSubTypeExtension(
                details.getPaymentSubTypeExtension());
        changePaymentRequest.changePaymentIn.setRecurringContinuously(
                details.isRecurringContinuously());
        changePaymentRequest.changePaymentIn.setRecurringFrequency(details.getRecurringFrequency());
        changePaymentRequest.changePaymentIn.setRecurringNumberOfPayments(
                details.getRecurringNumberOfPayments());
        changePaymentRequest.changePaymentIn.setStatusCode(details.getStatusCode());

        changePaymentRequest.changePaymentIn.setFromAccountId(fromAccount.get().getInternalId());

        // Logic based on that an eInvoice with BGType seems to have toAccountId set from
        // toAccountNumber
        if (!Strings.isNullOrEmpty(details.getToAccountId())) {
            changePaymentRequest.changePaymentIn.setToAccountId(details.getToAccountId());
        } else {
            changePaymentRequest.changePaymentIn.setToAccountId(details.getToAccountNumber());
        }

        // We don't store the recipient for Nordea accounts by default
        changePaymentRequest.changePaymentIn.setAddBeneficiary(false);

        // This is the default state for eInvoice changing, perhaps something else for other types
        changePaymentRequest.changePaymentIn.setDueDateTypeDueDatePayment();

        // In the Nordea app, when an eInvoice is modified, it's no longer an einvoice as type
        changePaymentRequest.changePaymentIn.setPaymentSubType(Payment.SubType.NORMAL);

        // Seems default to have no receipt on eInvoices
        changePaymentRequest.changePaymentIn.setReceiptCodeNoReceipt();

        // Guess this comes from if a payment should be stored as a template, does not seem to be
        // that for eInvoices
        changePaymentRequest.changePaymentIn.setStorePayment(false);

        return changePaymentRequest;
    }

    public void setAmount(Amount amount) {
        Preconditions.checkNotNull(amount);
        Preconditions.checkArgument(
                Objects.equal(amount.getCurrency(), "SEK"), "Only SEK transfers are supported");
        Preconditions.checkNotNull(amount.getValue(), "Cannot set empty amount on payments");

        changePaymentIn.setAmount(amount.getValue());
    }

    public void setDueDate(Date dueDate) {
        Preconditions.checkNotNull(dueDate);
        Preconditions.checkArgument(dueDate.after(new Date()), "Date must be set after today");

        changePaymentIn.setDueDate(dueDate);
    }

    public void setDestinationMessage(String destinationMessage) {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(destinationMessage), "Empty destination message");

        changePaymentIn.setMessageRow(destinationMessage);
    }
}
