package se.tink.backend.aggregation.utils.transfer;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableParametrizedEnum;
import se.tink.libraries.i18n.LocalizableParametrizedKey;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

/**
 * To be used in agents to in a common place do logic for default transfer messages. The agent
 * injects a configuration that set restriction for the formatter. Also catalog is injected since we
 * do translation of exception containing a user message.
 *
 * <p>The normalizer is an optional, but most banks only allow for a certain charset to be used as
 * message and at that point the normalizer can be used for ensuring validity of message.
 */
public class TransferMessageFormatter {
    private final Catalog catalog;
    private final TransferMessageLengthConfig messageLengthConfig;
    private final Optional<StringNormalizer> stringNormalizer;
    private static final String TINK_GENERATED_MESSAGE_FORMAT = "TinkGenerated://";

    public TransferMessageFormatter(
            Catalog catalog,
            TransferMessageLengthConfig bankTransferMessageLengthConfig,
            Optional<StringNormalizer> bankTransferStringNormalizer) {
        Preconditions.checkArgument(bankTransferStringNormalizer != null);

        this.catalog = catalog;
        this.messageLengthConfig = bankTransferMessageLengthConfig;
        this.stringNormalizer = bankTransferStringNormalizer;
    }

    public TransferMessageFormatter(
            Catalog catalog,
            TransferMessageLengthConfig bankTransferMessageLengthConfig,
            StringNormalizer bankTransferStringNormalizer) {
        Preconditions.checkArgument(bankTransferStringNormalizer != null);

        this.catalog = catalog;
        this.messageLengthConfig = bankTransferMessageLengthConfig;
        this.stringNormalizer = Optional.of(bankTransferStringNormalizer);
    }

    /**
     * Returns formatted source/destination message for a transfer to be commonly used in agents,
     * since agents differ on requirements on message strings.
     *
     * @param isTransferBetweenSameUserAccounts If the transfer source and destination belongs to
     *     same Tink user
     * @return Source/destination message that are formatted according to configuration of agent
     * @throws TransferMessageException When destination message doesn't apply to formatting, we
     *     throw to be able to tell the user to adjust his/her message. Though for source message
     *     (internal note) we just shorten it.
     */
    public Messages getMessagesFromRemittanceInformation(
            Transfer transfer, boolean isTransferBetweenSameUserAccounts)
            throws TransferMessageException {
        String destinationMessage =
                getDestinationMessageFromRemittanceInformation(
                        transfer, isTransferBetweenSameUserAccounts);
        String sourceMessage = getSourceMessage(transfer);

        return new Messages(sourceMessage, destinationMessage);
    }

    /**
     * Returns formatted destination message for a transfer to be commonly used in agents, since
     * agents differ on requirements on message strings.
     *
     * @param isTransferBetweenSameUserAccounts If the transfer source and destination belongs to
     *     same Tink user
     * @return Destination message that is formatted according to configuration of agent
     * @throws TransferMessageException When destination message doesn't apply to formatting, we
     *     throw to be able to tell the user to adjust his/her message.
     */
    public String getDestinationMessageFromRemittanceInformation(
            Transfer transfer, boolean isTransferBetweenSameUserAccounts)
            throws TransferMessageException {
        preconditionIsBankTransfer(transfer);

        int maxLength =
                messageLengthConfig.getDestinationMessageMaxLength(
                        isTransferBetweenSameUserAccounts);
        String destinationMessage =
                trimGeneratedText(transfer.getRemittanceInformation().getValue());

        if (transfer.isRemittanceInformationGenerated()) {
            return normalizeAndtrimMessageIfNeeded(destinationMessage, maxLength);
        }

        if (destinationMessage.length() > maxLength) {
            throw new TransferMessageException(
                    catalog.getString(
                            EndUserMessage.DESTINATION_MESSAGE_TO_LONG.cloneWith(maxLength)),
                    LogMessage.DESTINATION_MESSAGE_TO_LONG.with(transfer),
                    InternalStatus.DESTINATION_MESSAGE_TOO_LONG.toString());
        }

        Set<Character> normalizedCharacters = getCharactersBeingNormalized(destinationMessage);
        if (!normalizedCharacters.isEmpty()) {
            return createTransferExecutionException(transfer, normalizedCharacters);
        }

        return trim(destinationMessage);
    }

    private String trim(String message) {
        // Remove double spaces & spaces before/after text
        return message.replaceAll("\\s{2,}", " ").trim();
    }

    /**
     * Returns formatted source message for a transfer to be commonly used in agents, since agents
     * differ on requirements on message strings.
     *
     * @return Source message that is formatted according to configuration of agent
     */
    public String getSourceMessage(Transfer transfer) {
        preconditionIsBankTransfer(transfer);

        int maxLength = messageLengthConfig.getSourceMessageMaxLength();
        String sourceMessage = transfer.getSourceMessage();

        if (transfer.isSourceMessageGenerated()) {
            return normalizeAndtrimMessageIfNeeded(sourceMessage, maxLength);
        }

        if (sourceMessage.length() > maxLength) {
            throw new TransferMessageException(
                    catalog.getString(EndUserMessage.SOURCE_MESSAGE_TO_LONG.cloneWith(maxLength)),
                    LogMessage.SOURCE_MESSAGE_TO_LONG.with(transfer),
                    InternalStatus.SOURCE_MESSAGE_TOO_LONG.toString());
        }

        Set<Character> normalizedCharacters = getCharactersBeingNormalized(sourceMessage);
        if (!normalizedCharacters.isEmpty()) {
            return createTransferExecutionException(transfer, normalizedCharacters);
        }

        return trim(sourceMessage);
    }

    /**
     * Creates a set with the characters that was subject to normalization, so that we can assist
     * the user in showing the "bad" characters.
     *
     * <p>Note: The LinkedHashSet has ordering based on insertion (not on alphabetic sorting) which
     * is most logical for the user to see when comparing the original message.
     */
    private LinkedHashSet<Character> getCharactersBeingNormalized(String sourceMessage) {
        if (!stringNormalizer.isPresent()) {
            return Sets.newLinkedHashSet();
        }

        String normalizedMessage = stringNormalizer.get().normalize(sourceMessage);
        if (Objects.equal(normalizedMessage, sourceMessage)) {
            return Sets.newLinkedHashSet();
        }

        // From sourceMessage, remove all valid characters based on how the normalizedMessage looks
        // like
        // --> only the invalid chars are left in normalizedCharacters
        String normalizedCharacters =
                CharMatcher.anyOf(normalizedMessage).removeFrom(sourceMessage);

        // Extract all unique characters that were normalized
        LinkedHashSet<Character> diff = Sets.newLinkedHashSet();
        for (char character : normalizedCharacters.toCharArray()) {
            diff.add(character);
        }

        return diff;
    }

    /** Constructs exception with user message describing invalid characters used */
    private String createTransferExecutionException(
            Transfer transfer, Set<Character> normalizedCharacters) {
        String prettyFormatNormalizedCharacters = prettyFormat(normalizedCharacters);
        String allowedCharacters = stringNormalizer.get().getUnchangedCharactersHumanReadable();

        String logMessage =
                String.format(
                        LogMessage.MESSAGE_CONTAINS_INVALID_CHARACTERS.get(),
                        prettyFormatNormalizedCharacters,
                        transfer.toString());

        String userMessage =
                catalog.getString(
                        EndUserMessage.MESSAGE_CONTAINS_INVALID_CHARACTERS.cloneWith(
                                prettyFormatNormalizedCharacters, allowedCharacters));

        throw new TransferMessageException(userMessage, logMessage);
    }

    private String prettyFormat(Set<Character> normalizedCharacters) {
        return Joiner.on(" ").join(normalizedCharacters);
    }

    private String normalizeAndtrimMessageIfNeeded(String message, int maxLength) {
        if (stringNormalizer.isPresent()) {
            message = stringNormalizer.get().normalize(message);
        }

        if (message.length() > maxLength) {
            return message.substring(0, maxLength);
        }

        return trim(message);
    }

    private void preconditionIsBankTransfer(Transfer transfer) {
        if (!transfer.isOfType(TransferType.BANK_TRANSFER)) {
            throw new IllegalArgumentException(
                    "This class should only be used for transfers of type BANK_TRANSFER");
        }
    }

    public static class Messages {
        private final String sourceMessage;
        private final String destinationMessage;

        public Messages(String sourceMessage, String destinationMessage) {
            this.sourceMessage = sourceMessage;
            this.destinationMessage = destinationMessage;
        }

        public String getDestinationMessage() {
            return destinationMessage;
        }

        public String getSourceMessage() {
            return sourceMessage;
        }
    }

    public enum EndUserMessage implements LocalizableParametrizedEnum {
        DESTINATION_MESSAGE_TO_LONG(
                new LocalizableParametrizedKey(
                        "For the selected source, the destination message cannot be longer than {0} characters. Please adjust the message and try again.")),
        SOURCE_MESSAGE_TO_LONG(
                new LocalizableParametrizedKey(
                        "For the selected source, the destination message cannot be longer than {0} characters. Please adjust the message and try again.")),
        MESSAGE_CONTAINS_INVALID_CHARACTERS(
                new LocalizableParametrizedKey(
                        "For the selected source, messages cannot contain some of the typed characters:\n\n{0}\n\nValid characters:\n\n{1}"));

        private LocalizableParametrizedKey key;

        EndUserMessage(LocalizableParametrizedKey key) {
            this.key = key;
        }

        public LocalizableParametrizedKey getKey() {
            return key;
        }

        public LocalizableParametrizedKey cloneWith(Object... parameters) {
            return key.cloneWith(parameters);
        }
    }

    public enum LogMessage {
        DESTINATION_MESSAGE_TO_LONG("Too long destination message set for transfer: %s"),
        SOURCE_MESSAGE_TO_LONG("Too long source message set for transfer: %s"),
        MESSAGE_CONTAINS_INVALID_CHARACTERS("Invalid characters (%s) in message for transfer: %s");

        private String message;

        LogMessage(String message) {
            this.message = message;
        }

        public String with(Transfer parameter) {
            return String.format(message, parameter);
        }

        public String get() {
            return message;
        }
    }

    private String trimGeneratedText(String generatedMessage) {
        if (Strings.isNullOrEmpty(generatedMessage)) {
            return null;
        }
        return generatedMessage.replaceAll("^" + TINK_GENERATED_MESSAGE_FORMAT, "");
    }
}
