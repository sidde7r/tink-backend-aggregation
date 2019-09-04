package se.tink.libraries.identitydata.countries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EsIdentityDocumentTypeTest {

    @Test
    public void typeOf() {
        assertEquals(EsIdentityDocumentType.NIE, EsIdentityDocumentType.typeOf("Z2345678M"));
        assertEquals(EsIdentityDocumentType.NIE, EsIdentityDocumentType.typeOf("X2345677E"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("X2345677J"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("Z2345678Z"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("P2345678Y"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("12345678Y"));
        assertEquals(EsIdentityDocumentType.NIF, EsIdentityDocumentType.typeOf("12345678Z"));
        assertEquals(EsIdentityDocumentType.NIF, EsIdentityDocumentType.typeOf("12345677J"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("12345678Y"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("2837383"));
        assertEquals(EsIdentityDocumentType.OTHER, EsIdentityDocumentType.typeOf("J-221-21213-BQ"));
    }

    @Test
    public void isValidNie() {
        assertTrue(EsIdentityDocumentType.isValidNie("Z2345678M"));
        assertTrue(EsIdentityDocumentType.isValidNie("X2345677E"));
        assertTrue(EsIdentityDocumentType.isValidNie("X-2345677-E"));
        assertTrue(EsIdentityDocumentType.isValidNie("  X-2345677-E"));
        assertFalse(EsIdentityDocumentType.isValidNie("X2345677J"));
        assertFalse(EsIdentityDocumentType.isValidNie("Z2345678Z"));
        assertFalse(EsIdentityDocumentType.isValidNie("P2345678Y"));
        assertFalse(EsIdentityDocumentType.isValidNie("12345678Y"));
    }

    @Test
    public void isValidNif() {
        assertTrue(EsIdentityDocumentType.isValidNif("012345678Z"));
        assertTrue(EsIdentityDocumentType.isValidNif("12345678Z"));
        assertTrue(EsIdentityDocumentType.isValidNif("12345677J"));
        assertTrue(EsIdentityDocumentType.isValidNif("012345677J"));
        assertTrue(EsIdentityDocumentType.isValidNif("0-123-45678-Z "));
        assertFalse(EsIdentityDocumentType.isValidNif("12345678Y"));
        assertFalse(EsIdentityDocumentType.isValidNif("02345678Z"));
        assertFalse(EsIdentityDocumentType.isValidNif("X2345678Z"));
        assertFalse(EsIdentityDocumentType.isValidNif("X2345678E"));
    }
}
