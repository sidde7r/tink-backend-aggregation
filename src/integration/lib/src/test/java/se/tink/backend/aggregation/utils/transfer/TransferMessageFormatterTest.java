package se.tink.backend.aggregation.utils.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.base.Objects;
import java.util.Locale;
import java.util.Optional;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore
public class TransferMessageFormatterTest {

    private static RemittanceInformation getRemittanceInformation(
            RemittanceInformationType type, String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(value);
        remittanceInformation.setType(type);
        return remittanceInformation;
    }

    public static class MessageLength {
        private TransferMessageLengthConfig lengthConfig =
                TransferMessageLengthConfig.createWithMaxLength(25, 12, 25);
        private TransferMessageFormatter formatter =
                new TransferMessageFormatter(
                        Catalog.getCatalog("en-US"), lengthConfig, Optional.empty());

        private static final String LONG_MESSAGE =
                "Longest message ever written in the history of the world";
        private static final String VALID_SOURCE_MESSAGE = "Valid source message";
        private static final String VALID_DESTINATION_MESSAGE = "Message";

        private Transfer transfer = new Transfer();

        @Rule public ExpectedException expectedException = ExpectedException.none();

        @Before
        public void setup() {
            transfer.setType(TransferType.BANK_TRANSFER);
            transfer.setSourceMessage(VALID_SOURCE_MESSAGE);
            transfer.setRemittanceInformation(
                    getRemittanceInformation(
                            RemittanceInformationType.UNSTRUCTURED, VALID_DESTINATION_MESSAGE));
            transfer.setRemittanceInformation(
                    getRemittanceInformation(
                            RemittanceInformationType.UNSTRUCTURED, VALID_DESTINATION_MESSAGE));
        }

        @Test
        public void
                ensureGeneratedMessages_areNotTrimmed_whenMessagesWithinRange_forInternalTransfers() {
            transfer.setSourceMessage(VALID_SOURCE_MESSAGE);
            transfer.setRemittanceInformation(
                    getRemittanceInformation(
                            RemittanceInformationType.UNSTRUCTURED, VALID_SOURCE_MESSAGE));

            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, true);

            assertEquals(VALID_SOURCE_MESSAGE, messages.getSourceMessage());
            assertEquals(VALID_SOURCE_MESSAGE, messages.getDestinationMessage());
        }

        @Test
        public void ensureExceptionIsThrown_whenUserProvidedSourceMessage_isTooLong() {
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.SOURCE_MESSAGE_TO_LONG, 25);

            transfer.setSourceMessage(LONG_MESSAGE);
            formatter.getSourceMessage(transfer);
        }

        @Test
        public void
                ensureExceptionIsThrown_whenUserProvidedDestinationMessage_forExternalTransfer_isTooLong() {
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.DESTINATION_MESSAGE_TO_LONG, 12);

            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, LONG_MESSAGE));
            formatter.getDestinationMessageFromRemittanceInformation(transfer, false);
        }

        @Test
        public void
                ensureExceptionIsThrown_whenUserProvidedDestinationMessage_forInternalTransfer_isTooLong() {
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.DESTINATION_MESSAGE_TO_LONG, 25);

            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, LONG_MESSAGE));
            formatter.getDestinationMessageFromRemittanceInformation(transfer, true);
        }

        @Test
        public void
                ensureExceptionIsThrown_inCorrectOrder_whenUserProvidedDestinationMessage_isTooLong() {
            // The max for dest == 12 between external, source == 25.
            // The message should contain the dest limit when fetching both messages at same time
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.DESTINATION_MESSAGE_TO_LONG, 12);

            transfer.setSourceMessage(LONG_MESSAGE);
            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, LONG_MESSAGE));
            formatter.getMessagesFromRemittanceInformation(transfer, false);
        }

        private void expectTransferMessageException_withUserMessage(
                TransferMessageFormatter.EndUserMessage message, Object... args) {
            String localizableString = message.getKey().get();
            String expectedUserMessage = Catalog.format(localizableString, args);
            expectedException.expect(
                    TransferMessageExceptionMatcher.userMessageEquals(expectedUserMessage));
        }

        @Test
        public void
                ensureMessagesIsReturned_whenUserProvidedMessages_forExternalTransfer_isWithinLimit() {
            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, false);

            assertEquals(VALID_DESTINATION_MESSAGE, messages.getDestinationMessage());
            assertEquals(VALID_SOURCE_MESSAGE, messages.getSourceMessage());
        }

        @Test
        public void
                ensureMessageIsReturned_whenUserProvidedDestinationMessage_forInternalTransfer_isWithinLimit() {
            transfer.setRemittanceInformation(
                    getRemittanceInformation(
                            RemittanceInformationType.UNSTRUCTURED, VALID_SOURCE_MESSAGE));

            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, true);

            assertEquals(VALID_SOURCE_MESSAGE, messages.getSourceMessage());
            assertEquals(VALID_SOURCE_MESSAGE, messages.getDestinationMessage());
        }

        @Test(expected = IllegalArgumentException.class)
        public void ensureGetSourceMessageThrowsException_whenTransferType_isNot_BANK_TRANSFER() {
            transfer.setType(TransferType.EINVOICE);
            formatter.getSourceMessage(transfer);
        }

        @Test(expected = IllegalArgumentException.class)
        public void
                ensureGetDestinationMessageThrowsException_whenTransferType_isNot_BANK_TRANSFER() {
            transfer.setType(TransferType.EINVOICE);
            formatter.getDestinationMessageFromRemittanceInformation(transfer, false);
        }
    }

    public static class Normalize {
        private TransferMessageLengthConfig lengthConfig =
                TransferMessageLengthConfig.createWithMaxLength(999);

        @Test
        public void normalizerAbsent_doesntNormalizeUserMessages() {
            TransferMessageFormatter formatter =
                    new TransferMessageFormatter(null, lengthConfig, Optional.empty());

            Transfer transfer =
                    TransferMock.bankTransfer()
                            .withSourceMessage("source:123åäöüÅÄÖÜ$©@£")
                            .withRemittanceInformation(
                                    getRemittanceInformation(
                                            RemittanceInformationType.UNSTRUCTURED,
                                            "dest:123åäöüÅÄÖÜ$©@£"))
                            .build();

            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, false);

            assertThat(messages.getSourceMessage()).isEqualTo("source:123åäöüÅÄÖÜ$©@£");
            assertThat(messages.getDestinationMessage()).isEqualTo("dest:123åäöüÅÄÖÜ$©@£");
        }

        @Test
        public void normalizerAbsent_doesntNormalizeTinkGeneratedMessages() {
            TransferMessageFormatter formatter =
                    new TransferMessageFormatter(null, lengthConfig, Optional.empty());

            Transfer transfer = TransferMock.bankTransfer().build();
            transfer.setSourceMessage("source:123åäöüÅÄÖÜ$©@£");
            transfer.setRemittanceInformation(
                    getRemittanceInformation(
                            RemittanceInformationType.UNSTRUCTURED, "dest:123åäöüÅÄÖÜ$©@£"));

            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, false);

            assertThat(messages.getSourceMessage()).isEqualTo("source:123åäöüÅÄÖÜ$©@£");
            assertThat(messages.getDestinationMessage()).isEqualTo("dest:123åäöüÅÄÖÜ$©@£");
        }

        @Test
        @Ignore
        public void normalizerPresent_normalizesGeneratedMessages() {
            TransferMessageFormatter formatter =
                    new TransferMessageFormatter(
                            null, lengthConfig, new StringNormalizerSwedish(':'));

            Transfer transfer = TransferMock.bankTransfer().build();
            transfer.setSourceMessage("source:123åäöüÅÄÖÜ$©@£^");
            transfer.setRemittanceInformation(
                    getRemittanceInformation(
                            RemittanceInformationType.UNSTRUCTURED, "dest:123åäöüÅÄÖÜ$©@£"));
            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, false);

            assertThat(messages.getSourceMessage()).isEqualTo("source:123åäöuÅÄÖU");
            assertThat(messages.getDestinationMessage()).isEqualTo("dest:123åäöuÅÄÖU");
        }

        @Test(expected = TransferExecutionException.class)
        public void normalizerPresent_throwsWhenNormalizedSourceMessageDiffers() {
            try {
                TransferMessageFormatter formatter =
                        new TransferMessageFormatter(
                                Catalog.getCatalog(Locale.ENGLISH),
                                lengthConfig,
                                new StringNormalizerSwedish(':'));

                Transfer transfer =
                        TransferMock.bankTransfer()
                                .withSourceMessage("source:123åäöüÅÄÖÜ$©@£^è")
                                .build();

                formatter.getSourceMessage(transfer);
            } catch (TransferExecutionException e) {
                // The user message should contain the incorrect characters
                assertThat(e.getUserMessage()).contains("ü Ü $ © @ £ ^ è");
                // ...and as well the allowed character set
                assertThat(e.getUserMessage()).contains("a-ö A-Ö 0-9 :");
                assertThat(e.getMessage()).contains("ü Ü $ © @ £ ^ è");
                throw e;
            }
        }

        @Test(expected = TransferExecutionException.class)
        public void normalizerPresent_throwsWhenNormalizedDestinationMessageDiffers() {
            try {
                TransferMessageFormatter formatter =
                        new TransferMessageFormatter(
                                Catalog.getCatalog(Locale.ENGLISH),
                                lengthConfig,
                                new StringNormalizerSwedish(':'));

                Transfer transfer =
                        TransferMock.bankTransfer()
                                .withRemittanceInformation(
                                        getRemittanceInformation(
                                                RemittanceInformationType.UNSTRUCTURED,
                                                "dest:123åäöüÅÄÖÜ$©@£^è"))
                                .build();

                formatter.getDestinationMessageFromRemittanceInformation(transfer, false);
            } catch (TransferExecutionException e) {
                // The user message should contain the incorrect characters
                assertThat(e.getUserMessage()).contains("ü Ü $ © @ £ ^ è");
                // ...and as well the allowed character set
                assertThat(e.getUserMessage()).contains("a-ö A-Ö 0-9 :");
                assertThat(e.getMessage()).contains("ü Ü $ © @ £ ^ è");
                throw e;
            }
        }

        @Test
        @Ignore
        public void ensureDuplicateSpaces_betweenWords_areTrimmed_forGeneratedMessages() {
            TransferMessageFormatter formatter =
                    new TransferMessageFormatter(null, lengthConfig, new StringNormalizerSwedish());

            Transfer transfer = new Transfer();
            transfer.setType(TransferType.BANK_TRANSFER);
            transfer.setSourceMessage("Till A  B");
            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "Från B  C"));

            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, false);
            assertEquals("Till A B", messages.getSourceMessage());
            assertEquals("Från B C", messages.getDestinationMessage());

            transfer.setSourceMessage("Till A & B");
            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "Från B & C"));

            messages = formatter.getMessagesFromRemittanceInformation(transfer, false);
            assertEquals("Till A B", messages.getSourceMessage());
            assertEquals("Från B C", messages.getDestinationMessage());
        }

        @Test
        @Ignore
        public void ensureSpaces_beforeAndAfter_isRemoved_forGeneratedMessages() {
            TransferMessageFormatter formatter =
                    new TransferMessageFormatter(null, lengthConfig, new StringNormalizerSwedish());

            Transfer transfer = new Transfer();
            transfer.setType(TransferType.BANK_TRANSFER);
            transfer.setSourceMessage(" Till A ");
            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, " Från B "));

            TransferMessageFormatter.Messages messages =
                    formatter.getMessagesFromRemittanceInformation(transfer, false);
            assertEquals("Till A", messages.getSourceMessage());
            assertEquals("Från B", messages.getDestinationMessage());

            transfer.setSourceMessage(" Till A & ");
            transfer.setRemittanceInformation(
                    getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, " Från B & "));

            messages = formatter.getMessagesFromRemittanceInformation(transfer, false);
            assertEquals("Till A", messages.getSourceMessage());
            assertEquals("Från B", messages.getDestinationMessage());
        }
    }

    private static class TransferMessageExceptionMatcher
            extends TypeSafeMatcher<TransferMessageException> {
        private String expectedUserMessage;
        private String foundUserMessage;

        private static TransferMessageExceptionMatcher userMessageEquals(
                String expectedUserMessage) {
            TransferMessageExceptionMatcher transferMessageExceptionMatcher =
                    new TransferMessageExceptionMatcher();
            transferMessageExceptionMatcher.expectedUserMessage = expectedUserMessage;
            return transferMessageExceptionMatcher;
        }

        @Override
        protected boolean matchesSafely(TransferMessageException e) {
            this.foundUserMessage = e.getUserMessage();
            return Objects.equal(foundUserMessage, this.expectedUserMessage);
        }

        @Override
        public void describeTo(Description description) {
            description
                    .appendValue(foundUserMessage)
                    .appendText(" was found, which is not equal to expected ")
                    .appendValue(expectedUserMessage);
        }
    }
}
