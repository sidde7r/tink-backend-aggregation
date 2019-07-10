package se.tink.libraries.account.identifiers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NDAPersonalNumberIdentifierTest {

    @Test
    public void testNotValid() {

        NDAPersonalNumberIdentifier id1 = new NDAPersonalNumberIdentifier("900722");
        NDAPersonalNumberIdentifier id2 = new NDAPersonalNumberIdentifier("a9t42345");

        assertFalse(id1.isValid());
        assertFalse(id2.isValid());
    }

    @Test
    public void testValid() {

        NDAPersonalNumberIdentifier id1 = new NDAPersonalNumberIdentifier("9909121213");
        NDAPersonalNumberIdentifier id2 = new NDAPersonalNumberIdentifier("990912-1213");
        NDAPersonalNumberIdentifier id3 = new NDAPersonalNumberIdentifier("990912 1213");

        assertTrue(id1.isValid());
        assertTrue(id2.isValid());
        assertTrue(id2.isValid());
        assertEquals("9909121213", id1.getIdentifier());
        assertEquals("9909121213", id2.getIdentifier());
        assertEquals("9909121213", id3.getIdentifier());
    }
}
