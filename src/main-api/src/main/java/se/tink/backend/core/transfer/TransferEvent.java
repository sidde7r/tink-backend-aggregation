package se.tink.backend.core.transfer;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Strings;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.libraries.account.AccountIdentifier;

@Table(value = "transfers_events")
public class TransferEvent {

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private UUID transferId;

    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    private String remoteAddress;

    private Double amount;
    private String currency;
    private UUID credentialsId;
    private Date created;
    private String destination;
    private String originalDestination;
    private String destinationMessage;
    private String source;
    private String originalSource;
    private String sourceMessage;
    private String status;
    private String statusMessage;
    private Date updated;
    private String eventSource;
    private String transferType;
    private String messageType;

    public TransferEvent() {
        // Needed when loading data from cassandra
    }

    public TransferEvent(String eventSource, Transfer transfer, SignableOperation signableOperation, Optional<String> remoteAddress) {
        this.id = UUIDs.timeBased();
        this.userId = transfer.getUserId();
        this.remoteAddress = remoteAddress.orElse(null);
        this.eventSource = eventSource;

        // Copy all data from transfers
        this.transferId = transfer.getId();
        this.credentialsId = transfer.getCredentialsId();
        this.created = signableOperation.getCreated();
        this.destination = transfer.getDestination().toUriAsString();
        this.destinationMessage = transfer.getDestinationMessage();
        this.originalDestination = transfer.getOriginalDestination() != null ? transfer.getOriginalDestination()
                .toUriAsString() : null;
        this.originalSource = transfer.getOriginalSource() != null ? transfer.getOriginalSource().toUriAsString()
                : null;
        this.source = transfer.getSource().toUriAsString();
        this.sourceMessage = transfer.getSourceMessage();
        this.setTransferType(transfer.getType());
        this.setMessageType(transfer.getMessageType());
        this.status = signableOperation.getStatus().name();
        this.statusMessage = signableOperation.getStatusMessage();
        this.updated = signableOperation.getUpdated();

        Amount amount = transfer.getAmount();

        if (amount != null) {
            this.amount = amount.getValue();
            this.currency = amount.getCurrency();
        }
    }

    public AccountIdentifier getOriginalDestination() {
        if (originalDestination == null) {
            return null;
        }
        return AccountIdentifier.create(URI.create(originalDestination));
    }

    public AccountIdentifier getOriginalSource() {
        if (originalSource == null) {
            return null;
        }
        return AccountIdentifier.create(URI.create(originalSource));
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public SignableOperationStatuses getStatus() {
        if (status == null) {
            return null;
        }
        return SignableOperationStatuses.valueOf(status);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public Date getCreated() {
        return created;
    }

    public UUID getUserId() {
        return userId;
    }

    public Date getUpdated() {
        return updated;
    }

    public AccountIdentifier getDestination() {
        if (Strings.isNullOrEmpty(destination)) {
            return null;
        }
        return AccountIdentifier.create(URI.create(destination));
    }

    public AccountIdentifier getSource() {
        if (Strings.isNullOrEmpty(source)) {
            return null;
        }
        return AccountIdentifier.create(URI.create(source));
    }

    public Double getAmount() {
        return amount;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getCurrency() {
        return currency;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public String getEventSource() {
        return eventSource;
    }

    public TransferType getTransferType() {
        if (transferType == null) {
            return null;
        }
        return TransferType.valueOf(transferType);
    }

    public void setTransferType(TransferType transferType) {
        if (transferType == null) {
            this.transferType = null;
        } else {
            this.transferType = transferType.name();
        }
    }

    public MessageType getMessageType() {
        if (messageType == null) {
            return null;
        }
        return MessageType.valueOf(messageType);
    }

    public void setMessageType(MessageType messageType) {
        if (messageType == null) {
            this.messageType = null;
        } else {
            this.messageType = messageType.name();
        }
    }
}
