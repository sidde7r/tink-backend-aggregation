package se.tink.backend.aggregation.utils.transfer;

import com.google.common.base.Objects;
import java.util.Locale;
import java.util.Optional;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.libraries.helper.transfer.stubs.TransferStub;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.i18n.Catalog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class TransferMessageFormatterTest {
    public static class MessageLength {
        private TransferMessageLengthConfig lengthConfig = TransferMessageLengthConfig.createWithMaxLength(25, 12, 25);
        private TransferMessageFormatter formatter = new TransferMessageFormatter(Catalog.getCatalog("en-US"),
                lengthConfig, Optional.empty());

        private static final String LONG_MESSAGE = "Longest message ever written in the history of the world";
        private static final String VALID_SOURCE_MESSAGE = "Valid source message";
        private static final String VALID_DESTINATION_MESSAGE = "Message";

        private Transfer transfer = new Transfer();

        @Rule
        public ExpectedException expectedException = ExpectedException.none();

        @Before
        public void setup() {
            transfer.setType(TransferType.BANK_TRANSFER);
            transfer.setSourceMessage(VALID_SOURCE_MESSAGE);
            transfer.setDestinationMessage(VALID_DESTINATION_MESSAGE);
        }

        @Test
        public void ensureGeneratedMessages_areTrimmed_whenMessagesAreTooLong_forInternalTransfers() {
            transfer.setGeneratedSourceMessage(LONG_MESSAGE);
            transfer.setGeneratedDestinationMessage(LONG_MESSAGE);

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, true);

            assertTrue(messages.getSourceMessage().length() == 25);
            assertTrue(messages.getDestinationMessage().length() == 25);
        }

        @Test
        public void ensureGeneratedMessages_areTrimmed_whenMessagesAreTooLong_forExternalTransfers() {
            transfer.setGeneratedSourceMessage(LONG_MESSAGE);
            transfer.setGeneratedDestinationMessage(LONG_MESSAGE);

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);

            assertEquals(25, messages.getSourceMessage().length());
            assertEquals(12, messages.getDestinationMessage().length());
        }

        @Test
        public void ensureGeneratedMessages_areNotTrimmed_whenMessagesWithinRange_forInternalTransfers() {
            transfer.setGeneratedSourceMessage(VALID_SOURCE_MESSAGE);
            transfer.setGeneratedDestinationMessage(VALID_SOURCE_MESSAGE);

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, true);

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
        public void ensureExceptionIsThrown_whenUserProvidedDestinationMessage_forExternalTransfer_isTooLong() {
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.DESTINATION_MESSAGE_TO_LONG, 12);

            transfer.setDestinationMessage(LONG_MESSAGE);
            formatter.getDestinationMessage(transfer, false);
        }

        @Test
        public void ensureExceptionIsThrown_whenUserProvidedDestinationMessage_forInternalTransfer_isTooLong() {
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.DESTINATION_MESSAGE_TO_LONG, 25);

            transfer.setDestinationMessage(LONG_MESSAGE);
            formatter.getDestinationMessage(transfer, true);
        }

        @Test
        public void ensureExceptionIsThrown_inCorrectOrder_whenUserProvidedDestinationMessage_isTooLong() {
            // The max for dest == 12 between external, source == 25.
            // The message should contain the dest limit when fetching both messages at same time
            expectTransferMessageException_withUserMessage(
                    TransferMessageFormatter.EndUserMessage.DESTINATION_MESSAGE_TO_LONG, 12);

            transfer.setSourceMessage(LONG_MESSAGE);
            transfer.setDestinationMessage(LONG_MESSAGE);
            formatter.getMessages(transfer, false);

        }

        private void expectTransferMessageException_withUserMessage(
                TransferMessageFormatter.EndUserMessage message, Object... args) {
            String localizableString = message.getKey().get();
            String expectedUserMessage = Catalog.format(localizableString, args);
            expectedException.expect(TransferMessageExceptionMatcher.userMessageEquals(expectedUserMessage));
        }

        @Test
        public void ensureMessagesIsReturned_whenUserProvidedMessages_forExternalTransfer_isWithinLimit() {
            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);

            assertEquals(VALID_DESTINATION_MESSAGE, messages.getDestinationMessage());
            assertEquals(VALID_SOURCE_MESSAGE, messages.getSourceMessage());
        }

        @Test
        public void ensureMessageIsReturned_whenUserProvidedDestinationMessage_forInternalTransfer_isWithinLimit() {
            transfer.setDestinationMessage(VALID_SOURCE_MESSAGE);

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, true);

            assertEquals(VALID_SOURCE_MESSAGE, messages.getSourceMessage());
            assertEquals(VALID_SOURCE_MESSAGE, messages.getDestinationMessage());
        }

        @Test(expected = IllegalArgumentException.class)
        public void ensureGetSourceMessageThrowsException_whenTransferType_isNot_BANK_TRANSFER() {
            transfer.setType(TransferType.EINVOICE);
            formatter.getSourceMessage(transfer);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ensureGetDestinationMessageThrowsException_whenTransferType_isNot_BANK_TRANSFER() {
            transfer.setType(TransferType.EINVOICE);
            formatter.getDestinationMessage(transfer, false);
        }
    }

    public static class Normalize {
        private TransferMessageLengthConfig lengthConfig = TransferMessageLengthConfig.createWithMaxLength(999);

        @Test
        public void normalizerAbsent_doesntNormalizeUserMessages() {
            TransferMessageFormatter formatter = new TransferMessageFormatter(
                    null, lengthConfig, Optional.empty());

            Transfer transfer = TransferStub.bankTransfer()
                    .withSourceMessage("source:123åäöüÅÄÖÜ$©@£")
                    .withDestinationMessage("dest:123åäöüÅÄÖÜ$©@£")
                    .build();

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);

            assertThat(messages.getSourceMessage()).isEqualTo("source:123åäöüÅÄÖÜ$©@£");
            assertThat(messages.getDestinationMessage()).isEqualTo("dest:123åäöüÅÄÖÜ$©@£");
        }

        @Test
        public void normalizerAbsent_doesntNormalizeTinkGeneratedMessages() {
            TransferMessageFormatter formatter = new TransferMessageFormatter(
                    null, lengthConfig, Optional.empty());

            Transfer transfer = TransferStub.bankTransfer().build();
            transfer.setGeneratedSourceMessage("source:123åäöüÅÄÖÜ$©@£");
            transfer.setGeneratedDestinationMessage("dest:123åäöüÅÄÖÜ$©@£");

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);

            assertThat(messages.getSourceMessage()).isEqualTo("source:123åäöüÅÄÖÜ$©@£");
            assertThat(messages.getDestinationMessage()).isEqualTo("dest:123åäöüÅÄÖÜ$©@£");
        }

        @Test
        public void normalizerPresent_normalizesGeneratedMessages() {
            TransferMessageFormatter formatter = new TransferMessageFormatter(
                    null, lengthConfig, new StringNormalizerSwedish(':'));

            Transfer transfer = TransferStub.bankTransfer().build();
            transfer.setGeneratedSourceMessage("source:123åäöüÅÄÖÜ$©@£^");
            transfer.setGeneratedDestinationMessage("dest:123åäöüÅÄÖÜ$©@£");

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);

            assertThat(messages.getSourceMessage()).isEqualTo("source:123åäöuÅÄÖU");
            assertThat(messages.getDestinationMessage()).isEqualTo("dest:123åäöuÅÄÖU");
        }

        @Test(expected = TransferExecutionException.class)
        public void normalizerPresent_throwsWhenNormalizedSourceMessageDiffers() {
            try {
                TransferMessageFormatter formatter = new TransferMessageFormatter(
                        Catalog.getCatalog(Locale.ENGLISH),
                        lengthConfig, new StringNormalizerSwedish(':'));

                Transfer transfer = TransferStub.bankTransfer()
                        .withSourceMessage("source:123åäöüÅÄÖÜ$©@£^è").build();

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
                TransferMessageFormatter formatter = new TransferMessageFormatter(
                        Catalog.getCatalog(Locale.ENGLISH),
                        lengthConfig, new StringNormalizerSwedish(':'));

                Transfer transfer = TransferStub.bankTransfer()
                        .withDestinationMessage("dest:123åäöüÅÄÖÜ$©@£^è").build();

                formatter.getDestinationMessage(transfer, false);
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
        public void ensureDuplicateSpaces_betweenWords_areTrimmed_forGeneratedMessages() {
            TransferMessageFormatter formatter = new TransferMessageFormatter(null, lengthConfig,
                    new StringNormalizerSwedish());

            Transfer transfer = new Transfer();
            transfer.setType(TransferType.BANK_TRANSFER);
            transfer.setGeneratedSourceMessage("Till A  B");
            transfer.setGeneratedDestinationMessage("Från B  C");

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);
            assertEquals("Till A B", messages.getSourceMessage());
            assertEquals("Från B C", messages.getDestinationMessage());

            transfer.setGeneratedSourceMessage("Till A & B");
            transfer.setGeneratedDestinationMessage("Från B & C");

            messages = formatter.getMessages(transfer, false);
            assertEquals("Till A B", messages.getSourceMessage());
            assertEquals("Från B C", messages.getDestinationMessage());
        }

        @Test
        public void ensureSpaces_beforeAndAfter_isRemoved_forGeneratedMessages() {
            TransferMessageFormatter formatter = new TransferMessageFormatter(null, lengthConfig,
                    new StringNormalizerSwedish());

            Transfer transfer = new Transfer();
            transfer.setType(TransferType.BANK_TRANSFER);
            transfer.setGeneratedSourceMessage(" Till A ");
            transfer.setGeneratedDestinationMessage(" Från B ");

            TransferMessageFormatter.Messages messages = formatter.getMessages(transfer, false);
            assertEquals("Till A", messages.getSourceMessage());
            assertEquals("Från B", messages.getDestinationMessage());

            transfer.setGeneratedSourceMessage(" Till A & ");
            transfer.setGeneratedDestinationMessage(" Från B & ");

            messages = formatter.getMessages(transfer, false);
            assertEquals("Till A", messages.getSourceMessage());
            assertEquals("Från B", messages.getDestinationMessage());
        }
    }

    private static class TransferMessageExceptionMatcher extends TypeSafeMatcher<TransferMessageException> {
        private String expectedUserMessage;
        private String foundUserMessage;

        private static TransferMessageExceptionMatcher userMessageEquals(String expectedUserMessage) {
            TransferMessageExceptionMatcher transferMessageExceptionMatcher = new TransferMessageExceptionMatcher();
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
            description.appendValue(foundUserMessage)
                    .appendText(" was found, which is not equal to expected ")
                    .appendValue(expectedUserMessage);
        }
    }
}
