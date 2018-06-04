package se.tink.backend.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class CommonStringUtilsTest {

    @Test
    public void testCharsEscape() {
        String[] testWords = new String[] {
                "ttestupans Caf\\", ": 920109-0363", ":d 920109-0363", "?verf?ring :-",
                "?verf?ring K-?n 2:", "Annie :", "Bevakningstj{nst Basbt", "Bevakningstj{nst/BAS", "B{ckerei",
                "Debaser Humleg}r", "Din Biltv{tt M?l", "Drink:", "F{rgaren BA42", "Gutek{llaren", "Hanna:", "Hj?rta:",
                "J{rn AB S?dert", "J{rnia Sickla", "J{rnia Solna", "KFC R}dhuspladse", "Kulans Restaurang +", "Lisa:",
                "Lokal Ume}", "Pontus!", "Rb +", "Restaurang 28 +", "Sk}l Pub / CHARL", "Strandv{gen 1", "Svejtsch :",
                "Tack :-", "Taxitack :", "V{rdshuset", "Cafe-"
        };

        for (String s : testWords) {
            String escaped = CommonStringUtils.escapeElasticSearchSearchString(s);
            System.out.println("Unescaped:\t" + s);
            System.out.println("Escaped:\t" + escaped);
            System.out.println("");
            Assert.assertNotEquals(s, escaped);
        }
    }

}
