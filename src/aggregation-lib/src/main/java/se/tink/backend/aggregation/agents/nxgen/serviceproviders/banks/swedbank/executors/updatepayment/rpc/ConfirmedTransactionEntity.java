package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.AbstractExecutorTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.ToAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.TransferEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class ConfirmedTransactionEntity extends AbstractExecutorTransactionEntity {
    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(ConfirmedTransactionEntity.class);
    @JsonIgnore
    private static final String EMPTY_STRING = "";

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private Date bookedDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private Date date;
    private PaymentEntity payment;
    private String noteToSender;
    private List<String> scopes;

    public Date getBookedDate() {
        return bookedDate;
    }

    public Date getDate() {
        return date;
    }

    public PaymentEntity getPayment() {
        return payment;
    }

    public String getNoteToSender() {
        return noteToSender;
    }

    public List<String> getScopes() {
        return scopes;
    }

    @JsonIgnore
    public Optional<UpcomingTransaction> toTinkUpcomingTransaction(AccountIdentifier sourceAccount) {
        Amount amount = new Amount(currencyCode, StringUtils.parseAmountEU(this.amount));

        if (amount.isEmpty()) {
            return Optional.empty();
        }

        if (date == null) {
            return Optional.empty();
        }

        UpcomingTransaction.Builder upcomingTransactionBuilder = UpcomingTransaction.builder()
                .setAmount(amount)
                .setDate(date)
                .setDescription(getSourceMessage());

        Optional<Transfer> upcomingTransfer = getUpcomingTransfer(sourceAccount);
        if ("payment".equalsIgnoreCase(type) && upcomingTransfer.isPresent()) {
            upcomingTransactionBuilder.setUpcomingTransfer(upcomingTransfer.get());
        }

        return Optional.of(upcomingTransactionBuilder.build());
    }

    @JsonIgnore
    private String getDestinationMessage() {
        if (type == null) {
            return null;
        }

        switch (type.toLowerCase()) {
        case "transfer":
            return Optional.ofNullable(transfer)
                    .map(TransferEntity::getNoteToRecipient)
                    .orElse(EMPTY_STRING);
        case "payment":
            return Optional.ofNullable(payment)
                    .map(PaymentEntity::getReference)
                    .map(ReferenceEntity::getValue)
                    .orElse(EMPTY_STRING);
        default:
            log.warn("Unexpected transfer type: {}", type);
            return null;
        }
    }

    @JsonIgnore
    private String getSourceMessage() {
        if (type == null) {
            return null;
        }

        switch (type.toLowerCase()) {
        case "transfer":
            return Optional.ofNullable(noteToSender)
                    .filter(note -> !Strings.isNullOrEmpty(note))
                    .orElse(Optional.ofNullable(transfer)
                            .map(TransferEntity::getToAccount)
                            .map(ToAccountEntity::getName)
                            .orElse(EMPTY_STRING));
        case "payment":
            return Optional.ofNullable(payment)
                    .map(PaymentEntity::getPayee)
                    .map(PayeeEntity::getName)
                    .orElse(EMPTY_STRING);
        default:
            log.warn("Unexpected transfer type: {}", type);
            return null;
        }
    }

    @JsonIgnore
    private Optional<AccountIdentifier> getDestinationAccount() {
        if (type == null) {
            return Optional.empty();
        }

        switch (type.toLowerCase()) {
        case "transfer":
            return Optional.ofNullable(transfer)
                    .map(TransferEntity::getToAccount)
                    .map(ToAccountEntity::getFullyFormattedNumber)
                    .map(SwedishIdentifier::new);
        case "payment":
            return Optional.ofNullable(payment)
                    .map(PaymentEntity::getPayee)
                    .map(PayeeEntity::generalGetAccountIdentifier);
        default:
            log.warn("Unexpected transfer type: {}", type);
            return Optional.empty();
        }
    }

    @JsonIgnore
    private TransferType getTransferType() {
        if (type == null) {
            return null;
        }

        switch (type.toLowerCase()) {
        case "transfer":
            return TransferType.BANK_TRANSFER;
        case "payment":
            return TransferType.PAYMENT;
        default:
            log.warn("Unexpected transfer type: {}", type);
            return null;
        }
    }

    @JsonIgnore
    private Optional<Transfer> getUpcomingTransfer(AccountIdentifier sourceAccount) {
        Transfer transfer = new Transfer();

        transfer.setSourceMessage(getSourceMessage());
        transfer.setSource(sourceAccount);
        transfer.setDestinationMessage(getDestinationMessage());

        Optional<AccountIdentifier> destinationAccount = getDestinationAccount();
        if (!destinationAccount.isPresent()) {
            return Optional.empty();
        }

        if (!destinationAccount.get().isValid()) {
            return Optional.empty();
        }

        transfer.setDestination(destinationAccount.get());

        Amount amount = new Amount(currencyCode, StringUtils.parseAmountEU(this.amount));
        if (amount.isEmpty()) {
            return Optional.empty();
        }

        transfer.setAmount(amount);
        if (date == null) {
            return Optional.empty();
        }

        transfer.setDueDate(date);
        transfer.setType(getTransferType());

        return Optional.of(transfer);
    }
}
