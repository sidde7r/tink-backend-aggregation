package se.tink.backend.aggregation.agents.banks.seb.utilities;

import org.apache.commons.lang.time.DateFormatUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;

public class SEBAgentUtilsTest {

    @Test
    public void testTrimmedDashAgnosticEquals() {
        assertTrue(SEBAgentUtils.trimmedDashAgnosticEquals("123-125", "123125"));
        assertTrue(SEBAgentUtils.trimmedDashAgnosticEquals("123-125   ", "   1-23125"));
        assertTrue(SEBAgentUtils.trimmedDashAgnosticEquals(" 123-125 ", "123125"));
        assertFalse(SEBAgentUtils.trimmedDashAgnosticEquals("123-126", "123125"));
    }

    @Test
    public void testDescriptionParsing() throws Exception {
        SEBAgentUtils.DateAndDescriptionParser parser;

        parser = new SEBAgentUtils.DateAndDescriptionParser("2013-10-16", "MANDIRA INDI/13-10-14", null);
        parser.parse();
        assertEquals("MANDIRA INDI", parser.getDescription());
        assertEquals("2013-10-14", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));

        parser = new SEBAgentUtils.DateAndDescriptionParser("2013-10-16", "MANDIRA INDI", null);
        parser.parse();
        assertEquals("MANDIRA INDI", parser.getDescription());
        assertEquals("2013-10-16", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));

        parser = new SEBAgentUtils.DateAndDescriptionParser("2013-09-09", "646-470-8422/13-09-06",
                "IAC VIMEO PLUS           USD            248,75-   KURS 6,7919");
        parser.parse();
        // assertEquals("IAC VIMEO PLUS", parser.getDescription());
        assertEquals("646-470-8422", parser.getDescription());
        assertEquals("646-470-8422/13-09-06", parser.getOriginalDescription());
        assertEquals("2013-09-06", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));
    }

    @Test
    public void testAbroadParsing() throws Exception {
        SEBAgentUtils.AbroadTransactionParser parser;
        // Examples of description strings
        // KOZY KAR                 USD              9,00-   KURS 8,6911
        // CAFE KAFKA              2EUR6             3,20-41 KURS 9,6625
        // CITIZEN&IMM-EAPPS ENLIGN3CAD4             7,00-22 KURS 6,8657

        // Examples of region+date strings
        // BRUXELLES   /17-03-20
        // SAN FRANCISC/17-07-04

        // Test regular description and region.
        parser = new SEBAgentUtils.AbroadTransactionParser(
                "2017-03-22",
                "CAFE KAFKA              2EUR6             3,20-41 KURS 9,6625",
                "BRUXELLES   /17-03-20");
        parser.parse();
        assertEquals("CAFE KAFKA", parser.getDescription());
        assertEquals("EUR", parser.getLocalCurrency());
        assertEquals("-3.2", String.valueOf(parser.getLocalAmount()));
        assertEquals("9.6625", String.valueOf(parser.getExchangeRate()));
        assertEquals("BRUXELLES", parser.getRegion());
        assertEquals("2017-03-20", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));

        // Test region with full length (12 characters).
        parser = new SEBAgentUtils.AbroadTransactionParser(
                "2017-07-05",
                "KOZY KAR                 USD              9,00-   KURS 8,6911",
                "SAN FRANCISC/17-07-04");
        parser.parse();
        assertEquals("KOZY KAR", parser.getDescription());
        assertEquals("USD", parser.getLocalCurrency());
        assertEquals("-9.0", String.valueOf(parser.getLocalAmount()));
        assertEquals("8.6911", String.valueOf(parser.getExchangeRate()));
        assertEquals("SAN FRANCISC", parser.getRegion());
        assertEquals("2017-07-04", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));

        // Test description with full length description (24 characters).
        parser = new SEBAgentUtils.AbroadTransactionParser(
                "2017-06-28",
                "CITIZEN&IMM-EAPPS ENLIGN3CAD4             7,00 22 KURS 6,8657",
                "VANCOUVER   /17-06-27");
        parser.parse();
        assertEquals("CITIZEN&IMM-EAPPS ENLIGN", parser.getDescription());
        assertEquals("CAD", parser.getLocalCurrency());
        assertEquals("7.0", String.valueOf(parser.getLocalAmount()));
        assertEquals("6.8657", String.valueOf(parser.getExchangeRate()));
        assertEquals("VANCOUVER", parser.getRegion());
        assertEquals("2017-06-27", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));
    }

    @Test
    public void testSebBankIdErrorStatusParser() {
        assertEquals("EXPIRED_TRANSACTION", SEBAgentUtils.parseBankIdErrorCode(
                "Error: status=EXPIRED_TRANSACTION errorCode=RFA8 errorMessage=BankID-programmet svarar inte. Kontrollera att det Ã¤r startat och att du har internetanslutning. FÃ¶rsÃ¶k sedan igen."));
        assertEquals("Success", SEBAgentUtils.parseBankIdErrorCode("OK: status=Success errorCode= errorMessage="));
        assertEquals("USER_CANCEL", SEBAgentUtils
                .parseBankIdErrorCode("Error: status=USER_CANCEL errorCode=RFA6 errorMessage=Åtgärden avbruten."));
        assertEquals("CANCELLED", SEBAgentUtils.parseBankIdErrorCode(
                "Error: status=CANCELLED errorCode=RFA3 errorMessage=Ãtgärden avbruten. Försök igen."));

        // Haven't seen the messages bellow, but just making sure.

        assertEquals(null, SEBAgentUtils.parseBankIdErrorCode(
                "Error: errorCode=RFA3 errorMessage=Ãtgärden avbruten. Försök igen."));
        assertEquals("CANCELLED", SEBAgentUtils.parseBankIdErrorCode(
                "Error: status=CANCELLED errorMessage=Ãtgärden avbruten. Försök igen."));
        assertEquals("CANCELLED", SEBAgentUtils.parseBankIdErrorCode(
                "Error: errorCode=RFA3 status=CANCELLED errorMessage=Ãtgärden avbruten. Försök igen."));
        assertEquals(null, SEBAgentUtils.parseBankIdErrorCode(""));
        assertEquals(null, SEBAgentUtils.parseBankIdErrorCode(null));
    }

    @Test
    public void testSebTransactionTypes() {
        assertEquals(TransactionTypes.CREDIT_CARD, SEBAgentUtils.getTransactionType("5484398139"));
        assertEquals(TransactionTypes.CREDIT_CARD, SEBAgentUtils.getTransactionType("5484486056"));
        assertEquals(TransactionTypes.DEFAULT, SEBAgentUtils.getTransactionType("0000000000"));
        assertEquals(TransactionTypes.TRANSFER, SEBAgentUtils.getTransactionType("5490990004"));
        assertEquals(TransactionTypes.DEFAULT, SEBAgentUtils.getTransactionType(null));
    }
}
