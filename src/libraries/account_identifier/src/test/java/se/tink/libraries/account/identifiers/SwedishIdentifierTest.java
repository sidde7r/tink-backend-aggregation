package se.tink.libraries.account.identifiers;

import static org.junit.Assert.*;

import org.junit.Test;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

public class SwedishIdentifierTest {

    @Test
    public void testNotValid() {

        SwedishIdentifier id1 = new SwedishIdentifier("5192");
        SwedishIdentifier id2 = new SwedishIdentifier("a241g151243");
        SwedishIdentifier id3 = new SwedishIdentifier((String) null);
        SwedishIdentifier id4 = new SwedishIdentifier("");
        SwedishIdentifier id5 = new SwedishIdentifier("1245    --- -  ");

        assertFalse(id1.isValid());
        assertFalse(id2.isValid());
        assertFalse(id3.isValid());
        assertFalse(id4.isValid());
        assertFalse(id5.isValid());
    }

    @Test
    public void testValid() {

        SwedishIdentifier id1 = new SwedishIdentifier("6152-135 585 512");
        SwedishIdentifier id2 = new SwedishIdentifier("6152135585512");

        assertTrue(id1.isValid());
        assertTrue(id2.isValid());
        assertEquals("6152", id1.getClearingNumber());
        assertEquals("6152", id2.getClearingNumber());
        assertEquals("135585512", id1.getAccountNumber());
        assertEquals("135585512", id2.getAccountNumber());
        assertEquals("6152135585512", id1.getIdentifier());
        assertEquals("6152135585512", id2.getIdentifier());
    }

    @Test
    public void testSwedbankWithClearingNumberOfLength4() {

        SwedishIdentifier id1 = new SwedishIdentifier("7321-52-64235");
        SwedishIdentifier id2 = new SwedishIdentifier("73215264235");

        assertTrue(id1.isValid());
        assertTrue(id2.isValid());
        assertEquals("7321", id1.getClearingNumber());
        assertEquals("7321", id2.getClearingNumber());
        assertEquals("5264235", id1.getAccountNumber());
        assertEquals("5264235", id2.getAccountNumber());
        assertEquals("73215264235", id1.getIdentifier());
        assertEquals("73215264235", id2.getIdentifier());
    }

    @Test
    public void testSwedbankWithClearingNumberOfLength5() {
        SwedishIdentifier id1 = new SwedishIdentifier("8214-9,24 662 785-8");
        SwedishIdentifier id2 = new SwedishIdentifier("82149246627858");

        assertTrue(id1.isValid());
        assertTrue(id2.isValid());
        assertEquals("82149", id1.getClearingNumber());
        assertEquals("82149", id2.getClearingNumber());
        assertEquals("246627858", id1.getAccountNumber());
        assertEquals("246627858", id2.getAccountNumber());
        assertEquals("82149246627858", id1.getIdentifier());
        assertEquals("82149246627858", id2.getIdentifier());
    }

    @Test
    public void testDisplayIdentifier() {
        SwedishIdentifier id1 = new SwedishIdentifier("8214-9,24 662 785-8");
        SwedishIdentifier id2 = new SwedishIdentifier("7321-52-64235");

        assertEquals(
                "8214-9,246627858", id1.getIdentifier(new DisplayAccountIdentifierFormatter()));
        assertEquals("7321-5264235", id2.getIdentifier(new DisplayAccountIdentifierFormatter()));
    }

    @Test
    public void testIsGiroIdentifierShouldBeFalse() {

        SwedishIdentifier identifier = new SwedishIdentifier("7321-52-64235");
        assertFalse("SwedishIdentifier is not a GiroIdentifier", identifier.isGiroIdentifier());
    }
}
