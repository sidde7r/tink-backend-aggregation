package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing.utils;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.TransactionEntity;

public class DutchTextCleanerTest {

    @Test
    public void cleanUnstructuredInformationTest() {

        TransactionEntity transactionEntity = new TransactionEntity();

        final String ducthDescription_1 =
                "078 H&M ROTTERDAM ROTTERDAM NLD<br>Pasvolgnr: 007 05-03-2020 13:08<br>Transactie: M7V4C5 Term: ZU9602<br>Valutadatum: 06-03-2020";

        transactionEntity.setRemittanceInformationUnstructured(ducthDescription_1);

        final String expected_DucthDescription_1 = "H&m Rotterdam Nld";
        Assert.assertEquals(
                expected_DucthDescription_1,
                new DutchTextCleaner()
                        .clean(transactionEntity.getRemittanceInformationUnstructured()));

        final String ducthDescription_2 =
                "Naam: 12 VvE Albatros Lamellen 2-4 / 012<br>Omschrijving: Afrekening<br>jaarexploitatie 2017<br>IBAN: NL03INGB0678568316<br>Kenmerk: tbot25911166<br>Machtiging ID: 4288-75-0001<br>Incassant ID: NL67ZZZ505522360000<br>Doorlopende incasso<br>Valutadatum: 12-06-2018";

        transactionEntity.setRemittanceInformationUnstructured(ducthDescription_2);

        final String expected_DucthDescription_2 = "Vve Albatros Lamellen / Jaarexploitatie";

        Assert.assertEquals(
                expected_DucthDescription_2,
                new DutchTextCleaner()
                        .clean(transactionEntity.getRemittanceInformationUnstructured()));

        final String ducthDescription_3 =
                "3103 HFD Kiosk HOOFDDORP NLD<br>Pasvolgnr: 006 17-03-202013:15<br>Transactie: Y7A5N9 Term: 98VK01<br>Valutadatum: 18-03-2020";

        transactionEntity.setRemittanceInformationUnstructured(ducthDescription_3);

        final String expected_DucthDescription_3 = "Hfd Kiosk Hoofddorp Nld";

        Assert.assertEquals(
                expected_DucthDescription_3,
                new DutchTextCleaner()
                        .clean(transactionEntity.getRemittanceInformationUnstructured()));

        final String ducthDescription_4 =
                "7566 AKO Rotterdam ROTTERDAM NLD<br>Pasvolgnr: 009 28-02-2020 11:43<br>Transactie: W7Z9M5 Term: 5RPR6Z<br>Valutadatum: 29-02-2020";

        transactionEntity.setRemittanceInformationUnstructured(ducthDescription_4);

        final String expected_DucthDescription_4 = "Ako Rotterdam Nld";

        Assert.assertEquals(
                expected_DucthDescription_4,
                new DutchTextCleaner()
                        .clean(transactionEntity.getRemittanceInformationUnstructured()));

        final String ducthDescription_5 =
                "AEROPORT BARCELONA EL PRAT DE LL<br>Pasvolgnr: 001 26-02-2020 09:35<br>Transactie: N56332 Term: 03547980<br>Valutadatum: 27-02-2020";
        final String expected_DucthDescription_5 = "Aeroport Barcelona El Prat De Ll";

        transactionEntity.setRemittanceInformationUnstructured(ducthDescription_5);
        Assert.assertEquals(
                expected_DucthDescription_5,
                new DutchTextCleaner()
                        .clean(transactionEntity.getRemittanceInformationUnstructured()));
    }
}
