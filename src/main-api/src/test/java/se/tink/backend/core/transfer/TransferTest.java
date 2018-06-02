package se.tink.backend.core.transfer;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.backend.core.enums.TransferType;
import se.tink.libraries.date.DateUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest {
    private static final AccountIdentifier FIRST_SE_IDENTIFIER = new SwedishIdentifier("1200112233");
    private static final AccountIdentifier SECOND_SE_IDENTIFIER = new SwedishIdentifier("4295892751");
    private static final AccountIdentifier THIRD_SE_IDENTIFIER = new SwedishIdentifier("3201940285");
    private static final AccountIdentifier RIKSBANKEN = new PlusGiroIdentifier("20");
    private static final AccountIdentifier BG_IDENTIFIER = new BankGiroIdentifier("9020900");
    private static final AccountIdentifier PG_IDENTIFIER = new PlusGiroIdentifier("9020900");

    @Test
    public void sameHashWhenAmountDiffersAfterFourthDecimal() {
        Transfer first = new Transfer();
        first.setAmount(Amount.inSEK(123456.78901));

        Transfer second = new Transfer();
        second.setAmount(Amount.inSEK(123456.78902));

        assertThat(first.getHash()).isEqualTo(second.getHash());
    }

    @Test
    public void sameHashWhenAmountRoundsToSame() {
        Transfer first = new Transfer();
        first.setAmount(Amount.inSEK(123456.78901));

        Transfer second = new Transfer();
        second.setAmount(Amount.inSEK(123456.78899));

        assertThat(first.getHash()).isEqualTo(second.getHash());
    }

    @Test
    public void differentHashWhenAmountDiffersBeforeOrAtFourthDecimal() {
        Transfer first = new Transfer();
        first.setAmount(Amount.inSEK(123456.7891));

        Transfer second = new Transfer();
        second.setAmount(Amount.inSEK(123456.7892));

        assertThat(first.getHash()).isNotEqualTo(second.getHash());
    }

    @Test
    public void differentHashWhenAmountDiffersBecauseOfRounding() {
        Transfer first = new Transfer();
        first.setAmount(Amount.inSEK(123456.78901));

        Transfer second = new Transfer();
        second.setAmount(Amount.inSEK(123456.78909));

        assertThat(first.getHash()).isNotEqualTo(second.getHash());
    }

    @Test
    public void sameHashForDifferentNamesOnDestination() {
        Transfer first = new Transfer();
        first.setDestination(AccountIdentifier.create(URI.create("se-pg://9366006?name=Tele2+-+mobil")));

        Transfer second = new Transfer();
        second.setDestination(AccountIdentifier.create(URI.create("se-pg://9366006?name=TELE2+SVERIGE+AB%2FCOMVIQ")));

        assertThat(first.getHash()).isEqualTo(second.getHash());
    }

    @Test
    public void differentHashForDifferentDestinations() {
        Transfer first = new Transfer();
        first.setDestination(AccountIdentifier.create(URI.create("se://1200112233")));

        Transfer second = new Transfer();
        second.setDestination(AccountIdentifier.create(URI.create("se://1234112233")));

        assertThat(first.getHash()).isNotEqualTo(second.getHash());
    }

    @Test
    public void sameHashForDifferentNamesOnSource() {
        Transfer first = new Transfer();
        first.setSource(AccountIdentifier.create(URI.create("se-pg://9366006?name=Tele2+-+mobil")));

        Transfer second = new Transfer();
        second.setSource(AccountIdentifier.create(URI.create("se-pg://9366006?name=TELE2+SVERIGE+AB%2FCOMVIQ")));

        assertThat(first.getHash()).isEqualTo(second.getHash());
    }

    @Test
    public void differentHashForDifferentSources() {
        Transfer first = new Transfer();
        first.setSource(AccountIdentifier.create(URI.create("se://1200112233")));

        Transfer second = new Transfer();
        second.setSource(AccountIdentifier.create(URI.create("se://1234112233")));

        assertThat(first.getHash()).isNotEqualTo(second.getHash());
    }

    @Test
    public void ensureOnlyCreatableFields_isCopiedToNewTransfer() {
        Transfer incomingTransfer = makeEInvoice(FIRST_SE_IDENTIFIER, BG_IDENTIFIER);

        Transfer reliableTransfer = incomingTransfer.makeReliableCopy();

        ensureCreatableFieldsWasCopied(incomingTransfer, reliableTransfer);

        Assert.assertEquals(reliableTransfer.getType(), TransferType.PAYMENT);
        Assert.assertNotNull(reliableTransfer.getPayloadValue(TransferPayloadType.ORIGINAL_TRANSFER));
        Assert.assertNotEquals(reliableTransfer.getId(), incomingTransfer.getId());

        Assert.assertEquals(reliableTransfer.getOriginalSource(), incomingTransfer.getSource());
        Assert.assertEquals(reliableTransfer.getOriginalDestination(), incomingTransfer.getDestination());
    }

    @Test
    public void ensureOnlyModifiableFields_isCopiedToExistingTransfer() {
        final Transfer existingTransfer = makeEInvoice(FIRST_SE_IDENTIFIER, BG_IDENTIFIER);

        Transfer incomingTransfer = new Transfer();
        incomingTransfer.setAmount(Amount.inSEK(5000.23));
        incomingTransfer.setDueDate(DateUtils.addDays(new Date(), 2));
        incomingTransfer.setType(TransferType.BANK_TRANSFER);
        incomingTransfer.setSource(SECOND_SE_IDENTIFIER);
        incomingTransfer.setOriginalSource(THIRD_SE_IDENTIFIER);
        incomingTransfer.setSourceMessage("Updated source message");
        incomingTransfer.setDestination(RIKSBANKEN);
        incomingTransfer.setOriginalDestination(PG_IDENTIFIER);
        incomingTransfer.setDestinationMessage("Updated destination message");
        incomingTransfer.setPayload(makePayload(incomingTransfer));

        Transfer reliableTransfer = incomingTransfer.makeReliableCopy(existingTransfer);

        ensureModifiableFieldsWasCopied(incomingTransfer, reliableTransfer);

        Assert.assertEquals(reliableTransfer.getId(), existingTransfer.getId());
        Assert.assertNotNull(reliableTransfer.getPayloadValue(TransferPayloadType.ORIGINAL_TRANSFER));
        Assert.assertEquals(reliableTransfer.getType(), TransferType.EINVOICE);

        Assert.assertEquals(reliableTransfer.getOriginalSource(), incomingTransfer.getSource());
        Assert.assertEquals(reliableTransfer.getOriginalDestination(), incomingTransfer.getDestination());
    }

    @Test
    public void testGeneratedDestinationMessage() {
        Transfer transfer = new Transfer();

        String generatedMessage = "Source Name";
        transfer.setGeneratedDestinationMessage(generatedMessage);
        String destinationMessage = transfer.getDestinationMessage();

        Assert.assertTrue(transfer.isDestinationMessageGenerated());
        Assert.assertEquals(generatedMessage, destinationMessage);

        String userProvidedMessage = "Message provided by user";
        transfer.setDestinationMessage(userProvidedMessage);
        destinationMessage = transfer.getDestinationMessage();

        Assert.assertFalse(transfer.isDestinationMessageGenerated());
        Assert.assertEquals(userProvidedMessage, destinationMessage);
    }

    @Test
    public void testGeneratedSourceMessage() {
        Transfer transfer = new Transfer();

        String generatedMessage = "Destination Name";
        transfer.setGeneratedSourceMessage(generatedMessage);
        String sourceMessage = transfer.getSourceMessage();

        Assert.assertTrue(transfer.isSourceMessageGenerated());
        Assert.assertEquals(generatedMessage, sourceMessage);

        String userProvidedMessage = "Message provided by user";
        transfer.setSourceMessage(userProvidedMessage);
        sourceMessage = transfer.getSourceMessage();

        Assert.assertFalse(transfer.isSourceMessageGenerated());
        Assert.assertEquals(userProvidedMessage, sourceMessage);
    }

    /** Fields annotated with @Creatable:
     *  Amount, Destination, DestinationMessage, Source, SourceMessage, DueDate
     */
    private void ensureCreatableFieldsWasCopied(Transfer incomingTransfer, Transfer reliableTransfer) {
        ensureAnnotatedFieldsWasCopied(incomingTransfer, reliableTransfer);
    }

    /** Fields annotated with @Modifiable:
     *  Amount, Destination, DestinationMessage, Source, SourceMessage, DueDate
     */
    private void ensureModifiableFieldsWasCopied(Transfer incomingTransfer, Transfer reliableTransfer) {
        ensureAnnotatedFieldsWasCopied(incomingTransfer, reliableTransfer);
    }

    private void ensureAnnotatedFieldsWasCopied(Transfer incomingTransfer, Transfer reliableTransfer) {
        Assert.assertEquals(incomingTransfer.getAmount(), reliableTransfer.getAmount());
        Assert.assertEquals(incomingTransfer.getDestination(), reliableTransfer.getDestination());
        Assert.assertEquals(incomingTransfer.getDestinationMessage(), reliableTransfer.getDestinationMessage());
        Assert.assertEquals(incomingTransfer.getSource(), reliableTransfer.getSource());
        Assert.assertEquals(incomingTransfer.getSourceMessage(), reliableTransfer.getSourceMessage());
        Assert.assertEquals(incomingTransfer.getDueDate(), reliableTransfer.getDueDate());
    }

    private Transfer makeEInvoice(AccountIdentifier source, AccountIdentifier destination) {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(20.00));
        transfer.setType(TransferType.EINVOICE);
        transfer.setSource(source);
        transfer.setOriginalSource(source);
        transfer.setSourceMessage("Default source message");
        transfer.setDestination(destination);
        transfer.setOriginalDestination(destination);
        transfer.setDestinationMessage("Default destination message");
        transfer.setDueDate(new Date());
        transfer.setPayload(makePayload(transfer));

        return transfer;
    }

    private Map<TransferPayloadType, String> makePayload(Transfer transfer) {
        ObjectMapper mapper = new ObjectMapper();

        String originalTransfer;

        try {
            originalTransfer = mapper.writeValueAsString(transfer);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Could not deserialize transfer");
        }

        return makePayload(TransferPayloadType.ORIGINAL_TRANSFER, originalTransfer);
    }

    private Map<TransferPayloadType, String> makePayload(TransferPayloadType type, String value) {
        Map<TransferPayloadType, String> payload = Maps.newHashMap();
        payload.put(type, value);

        return payload;
    }
}
