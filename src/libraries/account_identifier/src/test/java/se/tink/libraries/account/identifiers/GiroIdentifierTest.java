package se.tink.libraries.account.identifiers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Optional;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

public class GiroIdentifierTest {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();

    @Test
    public void testIdentifierIsntValid() {
        assertFalse(new BankGiroIdentifier("1").isValid());
        assertFalse(new PlusGiroIdentifier("1").isValid());
        assertFalse(new BankGiroIdentifier("12345").isValid());
        assertFalse(new BankGiroIdentifier("").isValid());
        assertFalse(new BankGiroIdentifier(null).isValid());
        assertFalse(new PlusGiroIdentifier("").isValid());
        assertFalse(new PlusGiroIdentifier(null).isValid());
    }

    @Test
    public void testIdentifierIsValid() {
        assertTrue(new BankGiroIdentifier("1234567").isValid());
        assertTrue(new BankGiroIdentifier("12345678").isValid());
        assertTrue(new PlusGiroIdentifier("1234567").isValid());
        assertTrue(new PlusGiroIdentifier("42").isValid()); // SVERIGES RIKSBANK
        assertTrue(new PlusGiroIdentifier("123").isValid());
    }

    @Test
    public void testIdentifierReturnsCleanIdentifier() {
        assertEquals(
                "1234567", new BankGiroIdentifier("123-4567").getIdentifier(DEFAULT_FORMATTER));
        assertEquals(
                "12345678", new BankGiroIdentifier("1234-5678").getIdentifier(DEFAULT_FORMATTER));
        assertEquals(
                "1234567", new PlusGiroIdentifier("123456-7").getIdentifier(DEFAULT_FORMATTER));
        assertEquals("42", new PlusGiroIdentifier("4-2").getIdentifier(DEFAULT_FORMATTER));
    }

    @Test
    public void testIdentifierWithOcr() {
        BankGiroIdentifier bankGiroWithOcr = new BankGiroIdentifier("9020900", "1212121212");
        PlusGiroIdentifier plusGiroWithOcr = new PlusGiroIdentifier("9020900", "1212121212");

        assertEquals("9020900/1212121212", bankGiroWithOcr.getIdentifier());
        assertEquals("9020900", bankGiroWithOcr.getIdentifier(DEFAULT_FORMATTER));
        assertEquals(
                "902-0900", bankGiroWithOcr.getIdentifier(new DisplayAccountIdentifierFormatter()));

        assertEquals("9020900/1212121212", plusGiroWithOcr.getIdentifier());
        assertEquals("9020900", plusGiroWithOcr.getIdentifier(DEFAULT_FORMATTER));
        assertEquals(
                "902090-0", plusGiroWithOcr.getIdentifier(new DisplayAccountIdentifierFormatter()));
    }

    @Test
    public void identifierWithFaultyOcrIsNotValidAndHasNoIdentifier() {
        BankGiroIdentifier bankGiroWithOcr = new BankGiroIdentifier("9020900", "1212121210");
        PlusGiroIdentifier plusGiroWithOcr = new PlusGiroIdentifier("9020900", "1212121210");

        assertEquals(null, bankGiroWithOcr.getIdentifier());
        assertFalse(bankGiroWithOcr.isValid());

        assertEquals(null, plusGiroWithOcr.getIdentifier());
        assertFalse(plusGiroWithOcr.isValid());
    }

    @Test
    public void testIdentifierToUriWithOcr() {
        BankGiroIdentifier bankGiroWithOcr = new BankGiroIdentifier("9020900", "1212121212");
        PlusGiroIdentifier plusGiroWithOcr = new PlusGiroIdentifier("9020900", "1212121212");

        assertEquals("se-bg://9020900/1212121212", bankGiroWithOcr.toURI().toString());
        bankGiroWithOcr.setName("bgName");
        assertEquals("se-bg://9020900/1212121212?name=bgName", bankGiroWithOcr.toURI().toString());

        assertEquals("se-pg://9020900/1212121212", plusGiroWithOcr.toURI().toString());
        plusGiroWithOcr.setName("pgName");
        assertEquals("se-pg://9020900/1212121212?name=pgName", plusGiroWithOcr.toURI().toString());
    }

    @Test
    public void testCreateIdentifierWithOcrFromUri() {
        BankGiroIdentifier bankGiroWithOcr = new BankGiroIdentifier("9020900", "1212121212");
        bankGiroWithOcr.setName("bgName");
        bankGiroWithOcr =
                AccountIdentifier.create(bankGiroWithOcr.toURI()).to(BankGiroIdentifier.class);
        assertEquals("9020900/1212121212", bankGiroWithOcr.getIdentifier());
        assertEquals("9020900", bankGiroWithOcr.getIdentifier(DEFAULT_FORMATTER));
        assertEquals(
                "902-0900", bankGiroWithOcr.getIdentifier(new DisplayAccountIdentifierFormatter()));
        assertEquals(Optional.of("bgName"), bankGiroWithOcr.getName());
        assertEquals("se-bg://9020900/1212121212?name=bgName", bankGiroWithOcr.toURI().toString());

        PlusGiroIdentifier plusGiroWithOcr = new PlusGiroIdentifier("9020900", "1212121212");
        plusGiroWithOcr.setName("pgName");
        assertEquals("9020900/1212121212", plusGiroWithOcr.getIdentifier());
        assertEquals("9020900", plusGiroWithOcr.getIdentifier(DEFAULT_FORMATTER));
        assertEquals(
                "902090-0", plusGiroWithOcr.getIdentifier(new DisplayAccountIdentifierFormatter()));
        assertEquals(Optional.of("pgName"), plusGiroWithOcr.getName());
        assertEquals("se-pg://9020900/1212121212?name=pgName", plusGiroWithOcr.toURI().toString());
    }

    @Test
    public void testIdentifierTypeIsCorrect() {
        assertEquals(AccountIdentifier.Type.SE_BG, new BankGiroIdentifier("1234567").getType());
        assertEquals(AccountIdentifier.Type.SE_PG, new PlusGiroIdentifier("1234567").getType());
    }

    @Test
    public void testIdentifierNameIsCorrect() {
        AccountIdentifier identifier =
                AccountIdentifier.create(URI.create("se-bg://1234567?name=test"));

        assertEquals(AccountIdentifier.Type.SE_BG, identifier.getType());
        assertEquals("1234567", identifier.getIdentifier(DEFAULT_FORMATTER));
        assertEquals("test", identifier.getName().get());
    }

    @Test
    public void parsesPGURI() {
        URI uri = URI.create("se-pg://11112222");

        AccountIdentifier identifier = AccountIdentifier.create(uri);

        assertThat(identifier.toURI().toString()).isEqualTo("se-pg://11112222");
    }

    @Test
    public void parsesBGURI() {
        URI uri = URI.create("se-bg://1111222");

        AccountIdentifier identifier = AccountIdentifier.create(uri);

        assertThat(identifier.toURI().toString()).isEqualTo("se-bg://1111222");
    }

    @Test
    public void parsesIdentifierName() {
        URI uri = URI.create("se-bg://1111222?name=The+Name");

        AccountIdentifier identifier = AccountIdentifier.create(uri);

        assertThat(identifier.getName().isPresent()).isTrue();
        assertThat(identifier.getName().get()).isEqualTo("The Name");
    }

    @Test
    public void createsBGGiroIdentifierWithCorrectURI() {
        AccountIdentifier identifier = new BankGiroIdentifier("1111222");

        assertThat(identifier.toURI().toString()).isEqualTo("se-bg://1111222");
    }

    @Test
    public void createsPGGiroIdentifierWithCorrectURI() {
        AccountIdentifier identifier = new PlusGiroIdentifier("1111222");

        assertThat(identifier.toURI().toString()).isEqualTo("se-pg://1111222");
    }

    @Test
    public void isGiroIdentifierShouldReturnTrueForAllImplementations() {

        AccountIdentifier plusGiroIdentifier = new PlusGiroIdentifier("1111222");
        AccountIdentifier bankGiroIdentifier = new BankGiroIdentifier("1111222");

        assertTrue(plusGiroIdentifier.isGiroIdentifier());
        assertTrue(bankGiroIdentifier.isGiroIdentifier());
    }
}
