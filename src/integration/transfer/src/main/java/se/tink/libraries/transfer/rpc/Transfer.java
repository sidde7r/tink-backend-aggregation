package se.tink.libraries.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.iface.UuidIdentifiable;
import se.tink.libraries.uuid.UUIDUtils;

@SuppressWarnings("serial")
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transfer implements UuidIdentifiable, Serializable, Cloneable {
    private static final String FOUR_POINT_PRECISION_FORMAT_STRING = "0.0000";

    private static final String TINK_GENERATED_MESSAGE_FORMAT = "TinkGenerated://";
    private static final Logger log = LoggerFactory.getLogger(Transfer.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<TransferPayloadType, String>> PAYLOAD_TYPE_REFERENCE =
            new TypeReference<Map<TransferPayloadType, String>>() {};

    private BigDecimal amount;
    @JsonIgnore private Double oldAmount;
    private UUID credentialsId;
    private String currency;

    @JsonProperty("destinationUri")
    private String destination;

    @JsonIgnore private String originalDestination;
    private String destinationMessage;
    private UUID id;

    @JsonProperty("sourceUri")
    private String source;

    @JsonIgnore private String originalSource;
    private String sourceMessage;
    private UUID userId;
    private String type;
    private Date dueDate;
    private String payloadSerialized;
    private String originatingUserIp;

    @JsonProperty private RemittanceInformation remittanceInformation;

    public Transfer() {
        id = UUID.randomUUID();
    }

    @JsonIgnore
    public String getHash() {
        return getHash(false);
    }

    @JsonIgnore
    private String getHash(boolean ignoreSource) {
        AccountIdentifier sourceAccount = getSource();
        AccountIdentifier destinationAccount = getDestination();

        return String.valueOf(
                java.util.Objects.hash(
                        type,
                        getAmountForHash(amount),
                        destinationAccount != null ? destinationAccount.toURIWithoutName() : null,
                        destinationMessage,
                        // if ignoreSource is true, set source to null, otherwise use it if it
                        // exists
                        (ignoreSource || sourceAccount == null)
                                ? null
                                : sourceAccount.toURIWithoutName(),
                        sourceMessage,
                        dueDate == null
                                ? null
                                : ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate)));
    }

    /**
     * Doubles are not safe to do direct hashing on, so we convert it to a String first with defined
     * precision good enough for our purposes
     *
     * @return Four point precision of amount, or null if amount == null
     */
    private static String getAmountForHash(BigDecimal amount) {
        if (amount == null) {
            return null;
        }

        return new DecimalFormat(
                        FOUR_POINT_PRECISION_FORMAT_STRING,
                        DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                .format(amount);
    }

    public Amount getAmount() {
        if (amount != null) {
            return new Amount(currency, amount.doubleValue());
        } else if (oldAmount != null) {
            return new Amount(currency, oldAmount);
        }

        log.error(
                "[userId:{} credentialsId:{} transferId:{}] Transfer amount is null",
                UUIDUtils.toTinkUUID(userId),
                UUIDUtils.toTinkUUID(credentialsId),
                UUIDUtils.toTinkUUID(id));
        return new Amount(currency, null);
    }

    public void setAmount(Amount amount) {
        if (amount == null) {
            this.amount = null;
            this.oldAmount = null;
            this.currency = null;
        } else {
            this.amount = amount.toBigDecimal();
            this.oldAmount = amount.getValue();
            this.currency = amount.getCurrency();
        }
    }

    public void setAmount(ExactCurrencyAmount amount) {
        if (amount == null) {
            this.amount = null;
            this.oldAmount = null;
            this.currency = null;
        } else {
            this.amount = amount.getExactValue();
            this.oldAmount = amount.getDoubleValue();
            this.currency = amount.getCurrencyCode();
        }
    }

    public AccountIdentifier getOriginalDestination() {
        if (originalDestination == null) {
            return null;
        }
        return AccountIdentifier.create(URI.create(originalDestination));
    }

    public void setOriginalDestination(AccountIdentifier originalDestination) {
        if (originalDestination == null) {
            this.originalDestination = null;
        } else {
            this.originalDestination = originalDestination.toUriAsString();
        }
    }

    public AccountIdentifier getOriginalSource() {
        if (originalSource == null) {
            return null;
        }
        return AccountIdentifier.create(URI.create(originalSource));
    }

    public void setOriginalSource(AccountIdentifier originalSource) {
        if (originalSource == null) {
            this.originalSource = null;
        } else {
            this.originalSource = originalSource.toUriAsString();
        }
    }

    /**
     * @return Non-formatted destination message of transfer. For e.g. bank transfers message might
     *     need formatting and default values (use TransferMessageFormatter for this) to confirm to
     *     the specific agent.
     */
    public String getDestinationMessage() {
        return trimGeneratedText(destinationMessage);
    }

    public void setDestinationMessage(String destinationMessage) {
        this.destinationMessage = destinationMessage;
    }

    @JsonIgnore
    public void setGeneratedDestinationMessage(String generatedDestinationMessage) {
        this.destinationMessage = serializeGeneratedMessage(generatedDestinationMessage);
    }

    /**
     * @return Non-formatted source message of transfer. For e.g. bank transfers message might need
     *     formatting and default values (use TransferMessageFormatter for this) to confirm to the
     *     specific agent.
     */
    public String getSourceMessage() {
        return trimGeneratedText(sourceMessage);
    }

    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage;
    }

    @JsonIgnore
    public void setGeneratedSourceMessage(String generatedSourceMessage) {
        this.sourceMessage = serializeGeneratedMessage(generatedSourceMessage);
    }

    @Override
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

    @Override
    public String toString() {
        AccountIdentifier destinationAccount = getDestination();
        AccountIdentifier sourceAccount = getSource();
        String dateForHash =
                dueDate != null ? ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate) : null;

        return MoreObjects.toStringHelper(this.getClass())
                .add("amount", amount)
                .add("sourceAccount", sourceAccount)
                .add("sourceMessage", sourceMessage)
                .add("destinationAccount", destinationAccount)
                .add("destinationMessage", destinationMessage)
                .add("dueDate", dueDate)
                .add("type", type)
                .add("hash", getHash())
                .add("amountForHash", getAmountForHash(amount))
                .add("dueDateForHash", dateForHash)
                .toString();
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

    public Map<TransferPayloadType, String> getPayload() {
        try {
            if (Strings.isNullOrEmpty(payloadSerialized)) {
                return Maps.newHashMap();
            } else {
                return OBJECT_MAPPER.readValue(payloadSerialized, PAYLOAD_TYPE_REFERENCE);
            }
        } catch (IOException e) {
            log.error("Could not de-serialize payload.", e);
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
        } catch (JsonProcessingException e) {
            log.error("Could not serialize payload.", e);
        }
    }

    public void setRemittanceInformation(RemittanceInformation remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }

    public RemittanceInformation getRemittanceInformation() {
        return this.remittanceInformation;
    }

    @JsonIgnore
    public Optional<Transfer> getOriginalTransfer() {
        String originalTransferSerialized = getPayload().get(TransferPayloadType.ORIGINAL_TRANSFER);

        if (!Strings.isNullOrEmpty(originalTransferSerialized)) {
            return Optional.ofNullable(
                    SerializationUtils.deserializeFromString(
                            originalTransferSerialized, Transfer.class));
        }

        return Optional.empty();
    }

    @JsonIgnore
    public Optional<String> getPayloadValue(TransferPayloadType type) {
        Map<TransferPayloadType, String> payload = getPayload();
        return Optional.ofNullable(payload.get(type));
    }

    public void addPayload(TransferPayloadType type, String value) {
        Map<TransferPayloadType, String> payload = getPayload();
        payload.put(type, value);
        setPayload(payload);
    }

    @Override
    public Transfer clone() {
        try {
            return (Transfer) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @JsonIgnore
    public boolean isDestinationMessageGenerated() {
        return !Strings.isNullOrEmpty(destinationMessage) && isMessageGenerated(destinationMessage);
    }

    @JsonIgnore
    public boolean isSourceMessageGenerated() {
        return isMessageGenerated(sourceMessage);
    }

    private boolean isMessageGenerated(String message) {
        return message.startsWith(TINK_GENERATED_MESSAGE_FORMAT);
    }

    private String trimGeneratedText(String generatedMessage) {
        if (Strings.isNullOrEmpty(generatedMessage)) {
            return null;
        }
        return generatedMessage.replaceAll("^" + TINK_GENERATED_MESSAGE_FORMAT, "");
    }

    private String serializeGeneratedMessage(String message) {
        return TINK_GENERATED_MESSAGE_FORMAT + message;
    }

    @JsonIgnore
    public boolean isOfType(TransferType type) {
        return getType() != null && getType().equals(type);
    }

    @JsonIgnore
    public boolean isRemittanceInformationGenerated() {
        if (remittanceInformation != null) {
            return !Strings.isNullOrEmpty(remittanceInformation.getValue())
                    && isMessageGenerated(remittanceInformation.getValue());
        }
        return false;
    }

    public String getOriginatingUserIp() {
        return originatingUserIp;
    }

    public void setOriginatingUserIp(String originatingUserIp) {
        this.originatingUserIp = originatingUserIp;
    }
}
