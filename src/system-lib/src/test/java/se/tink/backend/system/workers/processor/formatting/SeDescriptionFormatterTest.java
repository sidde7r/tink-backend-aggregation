package se.tink.backend.system.workers.processor.formatting;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(JUnitParamsRunner.class)
public class SeDescriptionFormatterTest {
    static SeDescriptionFormatter formatter;

    @BeforeClass
    public static void setUp() {
        formatter = (SeDescriptionFormatter) MarketDescriptionFormatterFactory.byCluster(Cluster.TINK).get(null);
    }

    public void testGetCleanDescription(String input, String expected) {
        Transaction transaction = new Transaction();
        transaction.setDescription(null);
        transaction.setOriginalDescription(input);

        String got = formatter.getCleanDescription(transaction);
        assertThat(got).isEqualTo(expected);
    }

    @Test
    @Parameters({
            // Raw input   -   clean output
            "Sk}netrafiken, Skånetrafiken" ,
            "Fordonstj{nst I, Fordonstjänst I" ,
            "Ekebo N¦jescentr, Ekebo Nöjescentr" ,
            "GSk¨netrafiken, GSkånetrafiken" ,
            "H¯ssleholm, Hässleholm" ,
            "MALMÞ, MALMÖ",
            "SK»NETRAFIKEN, SKÅNETRAFIKEN" ,
            "RYDEB«CK, RYDEBÄCK" ,
            "ASSISTANCEK€REN SWEDEN\\,  SUNDBYBERG, ASSISTANCEKÅREN SWEDEN\\,  SUNDBYBERG" ,
            "ONOFF Sverige AB Borl¢nge, ONOFF Sverige AB Borlänge" ,
            "BOWLINGHALLEN\\,  STR§NGN§S, BOWLINGHALLEN\\,  STRÄNGNÄS" ,
            "KEMTV¬TTSGRUPPE, KEMTVÄTTSGRUPPE" ,
            "424 ÅHL±NS TRANÅS TRANÅS, 424 ÅHLÉNS TRANÅS TRANÅS" ,
            "þstgþta nation, östgöta nation",
    })
    // Test if our charset cleaning is working. Should replace broken characters with letter case in consideration.
    public void testSpecialCharacterCleaning(String input, String expected) {
        testGetCleanDescription(input, expected);
    }

    @Test
    @Parameters({
            "Crv*dressman, dressman",
            "crv*apoteket Duvan, apoteket Duvan",
            "nockeby Pizzeria, nockeby Pizzeria",
            "nockeby crv*Pizzeria, nockeby crv*Pizzeria",
    })
    public void testPrefixStripping(String input, String expected) {
        testGetCleanDescription(input, expected);
    }
}
