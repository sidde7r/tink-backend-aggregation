package se.tink.backend.aggregation.agents.legacy.banks.seb.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import se.tink.backend.aggregation.agents.legacy.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.legacy.banks.seb.SEBAgentUtils.AbroadTransactionParser;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n_aggregation.Catalog;

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

        String description = SEBAgentUtils.getParsedDescription("MANDIRA INDI/13-10-14");
        assertEquals("MANDIRA INDI", description);

        String description1 = SEBAgentUtils.getParsedDescription("MANDIRA INDI");
        assertEquals("MANDIRA INDI", description1);

        String description2 = SEBAgentUtils.getParsedDescription("646-470-8422/13-09-06");
        assertEquals("646-470-8422", description2);

        String description3 = SEBAgentUtils.getParsedDescription("646-470-8422   /13-09-06");
        assertEquals("646-470-8422", description2);

        Date flattenDate =
                DateUtils.flattenTime(DateUtils.parseDate("Thu Aug 08 00:00:00 UTC 2019"));
        assertEquals("Thu Aug 08 10:00:00 UTC 2019", flattenDate.toString());
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
        Date currentDate = new Date(2017, 07, 05);
        parser =
                new SEBAgentUtils.AbroadTransactionParser(
                        currentDate,
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
        parser =
                new SEBAgentUtils.AbroadTransactionParser(
                        currentDate,
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
        parser =
                new SEBAgentUtils.AbroadTransactionParser(
                        currentDate,
                        "CITIZEN&IMM-EAPPS ENLIGN3CAD4             7,00 22 KURS 6,8657",
                        "VANCOUVER   /17-06-27");
        parser.parse();
        assertEquals("CITIZEN&IMM-EAPPS ENLIGN", parser.getDescription());
        assertEquals("CAD", parser.getLocalCurrency());
        assertEquals("7.0", String.valueOf(parser.getLocalAmount()));
        assertEquals("6.8657", String.valueOf(parser.getExchangeRate()));
        assertEquals("VANCOUVER", parser.getRegion());
        assertEquals("2017-06-27", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));

        // Test description with characters before/after currency
        parser =
                new AbroadTransactionParser(
                        currentDate,
                        "JI HUA SHAN KAO YA      FCNYA         1.029,00-46 KURS 1,3403",
                        "BEIJING     /17-07-04");
        parser.parse();
        assertEquals("JI HUA SHAN KAO YA", parser.getDescription());
        assertEquals("CNY", parser.getLocalCurrency());
        assertEquals("-1029.0", String.valueOf(parser.getLocalAmount()));
        assertEquals("1.3403", String.valueOf(parser.getExchangeRate()));
        assertEquals("BEIJING", parser.getRegion());
        assertEquals("2017-07-04", DateFormatUtils.format(parser.getDate(), "yyyy-MM-dd"));
    }

    @Test
    public void testSebBankIdErrorStatusParser() {
        assertEquals(
                "EXPIRED_TRANSACTION",
                SEBAgentUtils.parseBankIdErrorCode(
                        "Error: status=EXPIRED_TRANSACTION errorCode=RFA8 errorMessage=BankID-programmet svarar inte. Kontrollera att det Ã¤r startat och att du har internetanslutning. FÃ¶rsÃ¶k sedan igen."));
        assertEquals(
                "Success",
                SEBAgentUtils.parseBankIdErrorCode("OK: status=Success errorCode= errorMessage="));
        assertEquals(
                "USER_CANCEL",
                SEBAgentUtils.parseBankIdErrorCode(
                        "Error: status=USER_CANCEL errorCode=RFA6 errorMessage=Åtgärden avbruten."));
        assertEquals(
                "CANCELLED",
                SEBAgentUtils.parseBankIdErrorCode(
                        "Error: status=CANCELLED errorCode=RFA3 errorMessage=Ãtgärden avbruten. Försök igen."));

        // Haven't seen the messages bellow, but just making sure.

        assertEquals(
                null,
                SEBAgentUtils.parseBankIdErrorCode(
                        "Error: errorCode=RFA3 errorMessage=Ãtgärden avbruten. Försök igen."));
        assertEquals(
                "CANCELLED",
                SEBAgentUtils.parseBankIdErrorCode(
                        "Error: status=CANCELLED errorMessage=Ãtgärden avbruten. Försök igen."));
        assertEquals(
                "CANCELLED",
                SEBAgentUtils.parseBankIdErrorCode(
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

    @Test
    public void testCorrectTranslationDep() {
        assertEquals(
                "Från-kontot är inte giltigt",
                Catalog.getCatalog("sv_SE").getString("Invalid source account"));
    }
}
