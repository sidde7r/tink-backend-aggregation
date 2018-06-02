package se.tink.backend.aggregationcontroller.v1.rpc.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.account.AccountIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("serial")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Transfer {
    private static final String TINK_GENERATED_MESSAGE_FORMAT = "TinkGenerated://";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<TransferPayloadType, String>> PAYLOAD_TYPE_REFERENCE =
            new TypeReference<Map<TransferPayloadType, String>>() { };

    private BigDecimal amount;
    private UUID credentialsId;
    private String currency;
    @JsonProperty("destinationUri")
    private String destination;
    private String destinationMessage;
    private UUID id;
    @JsonProperty("sourceUri")
    private String source;
    private String sourceMessage;
    private UUID userId;
    private String type;
    private Date dueDate;
    private String messageType;
    private String payloadSerialized;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDestinationMessage() {
        return trimGeneratedText(destinationMessage);
    }

    public void setDestinationMessage(String destinationMessage) {
        this.destinationMessage = destinationMessage;
    }

    public String getSourceMessage() {
        return trimGeneratedText(sourceMessage);
    }

    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AccountIdentifier getDestination() {
        if (Strings.isNullOrEmpty(destination)) {
            return null;
        }
        return AccountIdentifier.create(URI.create(destination));
    }

    public void setDestination(AccountIdentifier destination) {
        if (destination == null) {
            this.destination = null;
        } else {
            this.destination = destination.toUriAsString();
        }
    }

    public AccountIdentifier getSource() {
        if (Strings.isNullOrEmpty(source)) {
            return null;
        }
        return AccountIdentifier.create(URI.create(source));
    }

    public void setSource(AccountIdentifier source) {
        if (source == null) {
            this.source = null;
        } else {
            this.source = source.toUriAsString();
        }
    }

    public TransferType getType() {
        if (type != null) {
            return TransferType.valueOf(type);
        }
        return null;
    }

    public void setType(TransferType type) {
        if (type != null) {
            this.type = type.name();
        }
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }


    public MessageType getMessageType() {
        if (messageType != null) {
            return MessageType.valueOf(messageType);
        }
        return null;
    }

    public void setMessageType(MessageType messageType) {
        if (messageType != null) {
            this.messageType = messageType.name();
        }
    }

    public Map<TransferPayloadType, String> getPayload() {
        try {
            if (Strings.isNullOrEmpty(payloadSerialized)) {
                return Maps.newHashMap();
            } else {
                return OBJECT_MAPPER.readValue(payloadSerialized, PAYLOAD_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setPayload(Map<TransferPayloadType, String> payload) {
        try {
            if (payload == null) {
                this.payloadSerialized = null;
            } else {
                this.payloadSerialized = OBJECT_MAPPER.writeValueAsString(payload);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String trimGeneratedText(String generatedMessage) {
        if (Strings.isNullOrEmpty(generatedMessage)) {
            return null;
        }
        return generatedMessage.replaceAll("^" + TINK_GENERATED_MESSAGE_FORMAT, "");
    }
}
