package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.PaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class UpcomingPaymentEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty("Amount")
    private double amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @JsonProperty("Date")
    private Date date;

    @JsonProperty("EncryptedPaymentId")
    private String encryptedPaymentId;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @JsonProperty("HasError")
    private boolean hasError;

    @JsonProperty("HasInformation")
    private boolean hasInformation;

    @JsonProperty("InformationMessage")
    private String informationMessage;

    @JsonProperty("Invoice")
    private InvoiceEntity invoice;

    @JsonProperty("IsChangeable")
    private boolean isChangeable;

    @JsonProperty("IsDeletable")
    private boolean isDeletable;

    @JsonProperty("IsInvoice")
    private boolean isInvoice;

    @JsonProperty("PaymentType")
    private int paymentType;

    @JsonProperty("PaymentTypeDisplayName")
    private String paymentTypeDisplayName;

    @JsonProperty("PaymentTypeName")
    private String paymentTypeName;

    @JsonProperty("Recipient")
    private RecipientEntity recipient;

    @JsonProperty("Sender")
    private SenderEntity sender;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("StatusDisplayName")
    private String statusDisplayName;

    @JsonProperty("StatusName")
    private String statusName;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("TypeDisplayName")
    private String typeDisplayName;

    @JsonProperty("TypeName")
    private String typeName;

    @JsonIgnore
    public String getSenderAccountNumber() {
        return sender.getBankAccount().getBankAccountNumber();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.inSEK(amount).negate();
    }

    @JsonIgnore
    public boolean isApproved() {
        return statusName.equalsIgnoreCase(PaymentStatus.APPROVED);
    }

    private Optional<AccountIdentifier> getRecipientAccountIdentifier() {
        return SkandiaBankenConstants.PAYMENT_RECIPIENT_TYPE_MAP
                .translate(paymentTypeName)
                .map(
                        type ->
                                AccountIdentifier.create(
                                        type, recipient.getRecipientNumber(), recipient.getName()));
    }

    private AccountIdentifier getSenderAccountIdentifier() {
        return AccountIdentifier.create(
                AccountIdentifierType.SE,
                sender.getBankAccount().getBankAccountNumber(),
                sender.getName());
    }

    private Transfer toTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(getAmount());
        transfer.setDestination(getRecipientAccountIdentifier().orElse(null));
        transfer.setSource(getSenderAccountIdentifier());
        transfer.setType(
                SkandiaBankenConstants.PAYMENT_TRANSFER_TYPE_MAP
                        .translate(paymentTypeName)
                        .orElse(null));
        transfer.setSourceMessage(sender.getBankAccountTransactionNote());
        transfer.setDestinationMessage(recipient.getReference());
        return transfer;
    }

    public Optional<UpcomingTransaction> toTinkUpcomingTransaction() {
        final Transfer transfer = toTransfer();
        if (transfer.getDestination() == null || transfer.getType() == null) {
            logger.warn(
                    "tag={} upcoming payment - PaymentType={} PaymentTypeName={}",
                    LogTags.UPCOMING_TRANSFER,
                    paymentType,
                    paymentTypeName);
            return Optional.empty();
        }

        return Optional.of(
                UpcomingTransaction.builder()
                        .setAmount(getAmount())
                        .setDate(date)
                        .setDescription(sender.getBankAccountTransactionNote())
                        .setUpcomingTransfer(transfer)
                        .build());
    }
}
