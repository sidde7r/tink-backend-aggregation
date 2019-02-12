package se.tink.libraries.strings;

import com.google.common.collect.ImmutableList;
import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

    @Test
    public void testFormatHuman() {
        Assert.assertEquals("Testing 123", StringUtils.formatHuman("  \t TESTING 123 \u00a0"));
        Assert.assertEquals("Green Bean", StringUtils.formatHuman("GREEN   BEAN"));
        Assert.assertEquals("Green Bean", StringUtils.formatHuman("Green Bean"));
        Assert.assertEquals("www.aptit.se", StringUtils.formatHuman("www.aptit.se"));
        Assert.assertEquals("www.aptit.se", StringUtils.formatHuman("WWW.APTIT.SE"));
        Assert.assertEquals("WAY2SAVE CHECKING", StringUtils.stripExtendedAsciiCharacters("WAY2SAVE® CHECKING"));
        Assert.assertEquals("$account&savings", StringUtils.stripExtendedAsciiCharacters("$account&savings"));
        Assert.assertEquals("Shiro Sushi", StringUtils.formatHuman("Shiro Sushi HB"));
        Assert.assertEquals("Shiro Sushi", StringUtils.formatHuman("HB Shiro Sushi"));

        Assert.assertEquals("Lucky #749.san Carlos San Carlosca Xx9077",
                StringUtils.formatHuman("LUCKY #749.SAN CARLOS SAN CARLOSCA xx9077"));
        Assert.assertEquals("Columbus Data 02/19 #xxxxx7524 Withdrwl 511 Ocena Front W Venice Ca",
                StringUtils.formatHuman("COLUMBUS DATA 02/19 #xxxxx7524 WITHDRWL 511 OCENA FRONT W VENICE CA"));
        Assert.assertEquals("The Procter & Gamble Company, NYC",
                StringUtils.formatHuman("The Procter & Gamble Company, NYC"));

        byte[] weirdSalary = {
                0x00,
                (byte) 0x9D,
        };
        String weirdSalaryString = new String(weirdSalary, Charset.forName("utf16"));
        Assert.assertEquals(weirdSalaryString, StringUtils.formatHuman(weirdSalaryString));
    }

    @Test
    public void testUpperWeirdSalaryDescription() {
        byte[] weirdSalary = {
                0x00,
                (byte) 0x9D,
        };
        String weirdSalaryString = new String(weirdSalary, Charset.forName("utf16"));
        Assert.assertEquals(weirdSalaryString, weirdSalaryString.toUpperCase());
    }

    @Test
    public void testUpperWeirdSalaryAsUTF8() {
        byte[] weirdSalary = {
                (byte) 0xFE,
                (byte) 0xFF,
                0x00,
                (byte) 0x9D,
        };
        System.out.println("Originalbytes: ");
        printByteArray(weirdSalary);

        String weirdSalaryString = new String(weirdSalary, Charset.forName("utf16"));
        System.out.println("UF16: " + weirdSalaryString);

        System.out.println("utf8 bytes:");
        printByteArray(weirdSalaryString.getBytes(Charset.forName("utf8")));

        System.out.println("utf16 bytes:");
        printByteArray(weirdSalaryString.getBytes(Charset.forName("utf16")));

        String utf8WeirdSalary = new String(weirdSalaryString.getBytes(Charset.forName("utf8")),
                Charset.forName("utf8"));
        System.out.println("UTF8: " + utf8WeirdSalary);

        Assert.assertEquals(weirdSalaryString, utf8WeirdSalary);
    }

    private void printByteArray(byte[] weirdSalary) {
        for (byte b : weirdSalary) {
            System.out.println(" - " + String.format("%02X", b));
        }
    }
    
    @Test
    public void testTrim() {
        Assert.assertEquals("abc", StringUtils.trim("      abc      "));
    }
    
    @Test
    public void testTrimToNull() {
        Assert.assertNull(StringUtils.trimToNull(null));
        Assert.assertNull(StringUtils.trimToNull(""));
        Assert.assertNull(StringUtils.trimToNull("       "));
        Assert.assertEquals("abc", StringUtils.trimToNull("abc"));
        Assert.assertEquals("abc", StringUtils.trimToNull("      abc      "));
    }
    
    @Test
    public void testTrimTrailingNumbers() {
        Assert.assertEquals("HS ", StringUtils.trimTrailingDigits("HS 1234567"));
        Assert.assertEquals("HS", StringUtils.trimTrailingDigits("HS1234567"));
        Assert.assertEquals("HS-", StringUtils.trimTrailingDigits("HS-1234567"));
        Assert.assertEquals("", StringUtils.trimTrailingDigits("1234567"));
        Assert.assertEquals("Hello", StringUtils.trimTrailingDigits("Hello"));
        Assert.assertEquals("", StringUtils.trimTrailingDigits(""));
    }

    @Test
    public void testMaskSSN() {
        Assert.assertEquals("19821030****", StringUtils.maskSSN("198210303999"));
    }

    @Test
    public void testToUtf8FromIso() {
        Assert.assertEquals("aäaäaäaä", StringUtils.toUtf8FromIso("aÃ¤aÃ¤aÃ¤aÃ¤"));
        Assert.assertEquals("aaaaaaaa", StringUtils.toUtf8FromIso("aaaaaaaa"));
    }

    @Test
    public void testJoinStrings() {

        String comma = ", ";
        String and = " and ";

        assertThat(StringUtils.join(ImmutableList.of("bench press"), comma, and)).isEqualTo("bench press");

        assertThat(StringUtils.join(ImmutableList.of("bench press", "dead lift"), comma, and))
                .isEqualTo("bench press and dead lift");

        assertThat(StringUtils.join(ImmutableList.of("bench press", "dead lift", "squats"), comma, and))
                .isEqualTo("bench press, dead lift and squats");
    }
}
