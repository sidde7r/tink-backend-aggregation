package se.tink.backend.aggregation.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;
import org.junit.Test;
import se.tink.backend.aggregation.utils.eact.CompoundElement;
import se.tink.backend.aggregation.utils.eact.UnstructuredRemittanceInformation;

/* Test samples from document:
 * https://eact.eu/Core/Documents/Wordpress_Old/docs/EACT_Standard_for_Remittance_Info.pdf
 * Note: the samples in the document have a conflicting date format (the doc says YYYYMMDD, but
 * the samples have YYYYDDMM). I assumed the documented format is correct, and fixed the samples.
 * */
public class UnstructuredRemittanceInformationTest {
    @Test
    public void testSample1() throws ParseException {
        final String str = "/CNR/876543/DOC/894584334/DOC/894584335/ 45.56/ 20090727";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);
        assertEquals("876543", u1.getCustomerNumber().get());
        assertEquals(2, u1.getNumberOfDocumentReferences());

        final CompoundElement doc1 = u1.getDocumentReference(0);
        assertEquals("894584334", doc1.getReferenceNumber());
        assertFalse(doc1.getAmountPaid().isPresent());
        assertFalse(doc1.getDocumentDate().isPresent());

        final CompoundElement doc2 = u1.getDocumentReference(1);
        assertEquals("894584335", doc2.getReferenceNumber());
        assertEquals(new BigDecimal("45.56"), doc2.getAmountPaid().get());
        assertEquals("2009-07-27", doc2.getDocumentDate().get().toString());
    }

    @Test
    public void testSample2() throws ParseException {
        final String str =
                "/CNR/876543/DOC/94584334/DOC/94584335/ 45.56/ 20090727/DOC/94584336/ -34.10";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);
        assertEquals("876543", u1.getCustomerNumber().get());
        assertEquals(3, u1.getNumberOfDocumentReferences());

        final CompoundElement doc1 = u1.getDocumentReference(0);
        assertEquals("94584334", doc1.getReferenceNumber());
        assertFalse(doc1.getAmountPaid().isPresent());
        assertFalse(doc1.getDocumentDate().isPresent());

        final CompoundElement doc2 = u1.getDocumentReference(1);
        assertEquals("94584335", doc2.getReferenceNumber());
        assertEquals(new BigDecimal("45.56"), doc2.getAmountPaid().get());
        assertEquals("2009-07-27", doc2.getDocumentDate().get().toString());

        final CompoundElement doc3 = u1.getDocumentReference(2);
        assertEquals("94584336", doc3.getReferenceNumber());
        assertEquals(new BigDecimal("-34.10"), doc3.getAmountPaid().get());
        assertFalse(doc3.getDocumentDate().isPresent());
    }

    @Test
    public void testSample3() throws ParseException {
        final String str = "/CNR/876543/CINV/94584334/CREN/94584335";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);
        assertEquals("876543", u1.getCustomerNumber().get());

        assertEquals(1, u1.getNumberOfCommercialInvoices());
        final CompoundElement cinv1 = u1.getCommercialInvoice(0);
        assertEquals("94584334", cinv1.getReferenceNumber());
        assertFalse(cinv1.getAmountPaid().isPresent());
        assertFalse(cinv1.getDocumentDate().isPresent());

        assertEquals(1, u1.getNumberOfCreditNotes());
        final CompoundElement cren1 = u1.getCreditNote(0);
        assertEquals("94584335", cren1.getReferenceNumber());
        assertFalse(cren1.getAmountPaid().isPresent());
        assertFalse(cren1.getDocumentDate().isPresent());
    }

    @Test
    public void testSample4() throws ParseException {
        final String str = "/RFS/RF23567483937849450550875";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertEquals(1, u1.getNumberOfCheckedReferences());
        final CompoundElement rfs1 = u1.getCheckedReference(0);
        assertEquals("RF23567483937849450550875", rfs1.getReferenceNumber());
        assertFalse(rfs1.getAmountPaid().isPresent());
        assertFalse(rfs1.getDocumentDate().isPresent());
    }

    @Test
    public void testSample5() throws ParseException {
        final String str = "/RFB/9876096598656344";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertEquals(1, u1.getNumberOfUncheckedReferences());
        final CompoundElement rfb1 = u1.getUncheckedReference(0);
        assertEquals("9876096598656344", rfb1.getReferenceNumber());
        assertFalse(rfb1.getAmountPaid().isPresent());
        assertFalse(rfb1.getDocumentDate().isPresent());
    }

    @Test
    public void testSample6() throws ParseException {
        final String str = "/RFB/9876096598656344/ 45.56/ 20090727";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertEquals(1, u1.getNumberOfUncheckedReferences());
        final CompoundElement rfb1 = u1.getUncheckedReference(0);
        assertEquals("9876096598656344", rfb1.getReferenceNumber());
        assertEquals(new BigDecimal("45.56"), rfb1.getAmountPaid().get());
        assertEquals("2009-07-27", rfb1.getDocumentDate().get().toString());
    }

    @Test
    public void testSample7() throws ParseException {
        final String str = "/PUR/SAL/TXT/salary number 1234578 November 2009";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getPurpose().isPresent());
        assertEquals("SAL", u1.getPurpose().get());
        assertTrue(u1.getFreeText().isPresent());
        assertEquals("salary number 1234578 November 2009", u1.getFreeText().get());
    }

    @Test
    public void testSample8() throws ParseException {
        final String str = "/URI/8798877/URL/mailbox@system.company.com";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getUri().isPresent());
        assertEquals("8798877", u1.getUri().get());
        assertTrue(u1.getUrl().isPresent());
        assertEquals("mailbox@system.company.com", u1.getUrl().get());
    }

    @Test
    public void testSample9() throws ParseException {
        final String str = "/CNR/876543/TXT/ADVANCED PAYMENT FOR PROJECT SAUDI ARABIA/TELECOM";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getCustomerNumber().isPresent());
        assertEquals("876543", u1.getCustomerNumber().get());
        assertTrue(u1.getFreeText().isPresent());
        assertEquals("ADVANCED PAYMENT FOR PROJECT SAUDI ARABIA/TELECOM", u1.getFreeText().get());
    }

    @Test
    public void testBankinterSample1() throws ParseException {
        final String str =
                "/DOC/acff513e-521b-4a69-858f-3b3ccf574e2a/TXT/D|LIQUID. CUOTA PTMO.  123456789";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertEquals(1, u1.getNumberOfDocumentReferences());
        final CompoundElement doc1 = u1.getDocumentReference(0);
        assertEquals("acff513e-521b-4a69-858f-3b3ccf574e2a", doc1.getReferenceNumber());
        assertFalse(doc1.getAmountPaid().isPresent());
        assertFalse(doc1.getDocumentDate().isPresent());

        assertTrue(u1.getFreeText().isPresent());
        assertEquals("D|LIQUID. CUOTA PTMO.  123456789", u1.getFreeText().get());
    }

    @Test
    public void testBankinterSample2() throws ParseException {
        final String str =
                "/DOC/acff513e-521b-4a69-858f-3b3ccf574e2a/TXT/D|RECIB /CANAL DE ISABEL II, S.A";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertEquals(1, u1.getNumberOfDocumentReferences());
        final CompoundElement doc1 = u1.getDocumentReference(0);
        assertEquals("acff513e-521b-4a69-858f-3b3ccf574e2a", doc1.getReferenceNumber());
        assertFalse(doc1.getAmountPaid().isPresent());
        assertFalse(doc1.getDocumentDate().isPresent());

        assertTrue(u1.getFreeText().isPresent());
        assertEquals("D|RECIB /CANAL DE ISABEL II, S.A", u1.getFreeText().get());
    }

    @Test
    public void testBankiaSample1() throws ParseException {
        final String str =
                "/TXT/POR SER TU, DEVOLUCION POR CUOTA TARJETA|POR SER TU, DEVOLUCION POR CUOTA TARJETA/CNR/CENTRAL DE TARJETAS (1234)|123456789012";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getCustomerNumber().isPresent());
        assertEquals("CENTRAL DE TARJETAS (1234)|123456789012", u1.getCustomerNumber().get());
        assertTrue(u1.getFreeText().isPresent());
        assertEquals(
                "POR SER TU, DEVOLUCION POR CUOTA TARJETA|POR SER TU, DEVOLUCION POR CUOTA TARJETA",
                u1.getFreeText().get());
    }

    @Test
    public void testBankiaSample2() throws ParseException {
        final String str =
                "/TXT/RESTAURANTE IKEA S|COMPRA COMERCIO/CNR/RESTAURANTE IKEA SEVILLA|111122223333";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getCustomerNumber().isPresent());
        assertEquals("RESTAURANTE IKEA SEVILLA|111122223333", u1.getCustomerNumber().get());
        assertTrue(u1.getFreeText().isPresent());
        assertEquals("RESTAURANTE IKEA S|COMPRA COMERCIO", u1.getFreeText().get());
    }

    @Test
    public void testBankiaSample3() throws ParseException {
        final String str = "/TXT/TRANSFERENCIA DE FULANO GARCIA DE TAL";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getFreeText().isPresent());
        assertEquals("TRANSFERENCIA DE FULANO GARCIA DE TAL", u1.getFreeText().get());
    }

    @Test
    public void testBankiaSample4() throws ParseException {
        final String str = "/TXT/TRANSFERENCIA DE FULANO GARCIA DE TAL                1234";
        assertTrue(UnstructuredRemittanceInformation.matches(str));
        final UnstructuredRemittanceInformation u1 = UnstructuredRemittanceInformation.parse(str);

        assertTrue(u1.getFreeText().isPresent());
        assertEquals(
                "TRANSFERENCIA DE FULANO GARCIA DE TAL                1234",
                u1.getFreeText().get());
    }

    @Test
    public void testNonMatching() {
        final String str1 = "ADVANCED PAYMENT FOR PROJECT SAUDI ARABIA/TELECOM";
        assertFalse(UnstructuredRemittanceInformation.matches(str1));
    }
}
