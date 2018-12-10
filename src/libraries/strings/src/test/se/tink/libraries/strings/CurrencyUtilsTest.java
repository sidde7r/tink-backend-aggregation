package se.tink.libraries.strings;

import org.junit.Assert;
import org.junit.Test;

public class CurrencyUtilsTest {
    @Test
    public void testParseAmount() {
        double epsilon = 0;
        
        Assert.assertEquals(-123, StringUtils.parseAmount("-123."), epsilon);
        Assert.assertEquals(-123, StringUtils.parseAmount("-123,"), epsilon);
        Assert.assertEquals(-123, StringUtils.parseAmount("-123.0"), epsilon);
        Assert.assertEquals(-123, StringUtils.parseAmount("-123,0"), epsilon);
        Assert.assertEquals(-123.4, StringUtils.parseAmount("-123.4"), epsilon);
        Assert.assertEquals(-123.4, StringUtils.parseAmount("-123,4"), epsilon);
        Assert.assertEquals(-123.45, StringUtils.parseAmount("-123.45"), epsilon);
        Assert.assertEquals(-123.45, StringUtils.parseAmount("-123,45"), epsilon);
        
        Assert.assertEquals(-12345.67, StringUtils.parseAmount("-12,345.67"), epsilon);
        Assert.assertEquals(-12345.67, StringUtils.parseAmount("-12.345,67"), epsilon);
        Assert.assertEquals(-12345.67, StringUtils.parseAmount("-12 345,67"), epsilon);
        Assert.assertEquals(-12345.67, StringUtils.parseAmount("-12'345,67"), epsilon);
        
        Assert.assertEquals(-12345.678, StringUtils.parseAmount("-12'345,678"), epsilon);
        Assert.assertEquals(-12345.678, StringUtils.parseAmount("-12 345,678"), epsilon);
        Assert.assertEquals(-12345678, StringUtils.parseAmount("-12,345,678"), epsilon);
        Assert.assertEquals(-12345678, StringUtils.parseAmount("-12.345.678"), epsilon);
        
        Assert.assertEquals(-1234567, StringUtils.parseAmount("-1 234 567"), epsilon);
        
        Assert.assertEquals(0.1, StringUtils.parseAmount(".1"), epsilon);
        Assert.assertEquals(0.12, StringUtils.parseAmount(".12"), epsilon);
        Assert.assertEquals(0.123, StringUtils.parseAmount(".123"), epsilon);
        Assert.assertEquals(0.1234, StringUtils.parseAmount(".1234"), epsilon);
        Assert.assertEquals(-0.1, StringUtils.parseAmount("-.1"), epsilon);
        Assert.assertEquals(-0.12, StringUtils.parseAmount("-.12"), epsilon);
        Assert.assertEquals(-0.123, StringUtils.parseAmount("-.123"), epsilon);
        Assert.assertEquals(-0.1234, StringUtils.parseAmount("-.1234"), epsilon);
        Assert.assertEquals(20000, StringUtils.parseAmount("20 000,00"), epsilon);
        Assert.assertEquals(0, StringUtils.parseAmount(""), epsilon);
    }

    @Test
    public void testParseAmountUS(){

        double epsilon = 0;

        Assert.assertEquals(-123, StringUtils.parseAmountUS("-123."), epsilon);
        Assert.assertEquals(-123, StringUtils.parseAmountUS("-123.0"), epsilon);
        Assert.assertEquals(-123.4, StringUtils.parseAmountUS("-123.4"), epsilon);
        Assert.assertEquals(-123.45, StringUtils.parseAmountUS("-123.45"), epsilon);

        Assert.assertEquals(-12345.67, StringUtils.parseAmountUS("-12,345.67"), epsilon);


        Assert.assertEquals(-1234567, StringUtils.parseAmountUS("-1 234 567"), epsilon);

        Assert.assertEquals(0.1, StringUtils.parseAmountUS(".1"), epsilon);
        Assert.assertEquals(0.12, StringUtils.parseAmountUS(".12"), epsilon);
        Assert.assertEquals(-0.1, StringUtils.parseAmountUS("-.1"), epsilon);
        Assert.assertEquals(-0.12, StringUtils.parseAmountUS("-.12"), epsilon);
        Assert.assertEquals(0, StringUtils.parseAmountUS(""), epsilon);

        Assert.assertEquals(-3000, StringUtils.parseAmountUS("-3,000"), epsilon);
        Assert.assertEquals(-3000, StringUtils.parseAmountUS("-3,000.00"), epsilon);

        Assert.assertEquals(3000, StringUtils.parseAmountUS(" 3,000.00"), epsilon);
        Assert.assertEquals(-3000, StringUtils.parseAmountUS(" -3,000.00"), epsilon);
        Assert.assertEquals(3000, StringUtils.parseAmountUS("d3,000.00"), epsilon);
        Assert.assertEquals(-3000, StringUtils.parseAmountUS("d-3,000.00"), epsilon);

    }
    @Test
    public void testParseAmountEU(){

        double epsilon = 0;

        Assert.assertEquals(-123, StringUtils.parseAmountEU("-123,"), epsilon);
        Assert.assertEquals(-123, StringUtils.parseAmountEU("-123,0"), epsilon);
        Assert.assertEquals(-123.4, StringUtils.parseAmountEU("-123,4"), epsilon);
        Assert.assertEquals(-123.45, StringUtils.parseAmountEU("-123,45"), epsilon);

        Assert.assertEquals(-12345.67, StringUtils.parseAmountEU("-12,345,67"), epsilon);


        Assert.assertEquals(-1234567, StringUtils.parseAmountEU("-1 234 567"), epsilon);

        Assert.assertEquals(0.1, StringUtils.parseAmountEU(",1"), epsilon);
        Assert.assertEquals(0.12, StringUtils.parseAmountEU(",12"), epsilon);
        Assert.assertEquals(-0.1, StringUtils.parseAmountEU("-,1"), epsilon);
        Assert.assertEquals(-0.12, StringUtils.parseAmountEU("-,12"), epsilon);
        Assert.assertEquals(0, StringUtils.parseAmountEU(""), epsilon);

        Assert.assertEquals(-3000, StringUtils.parseAmountEU("-3.000"), epsilon);
        Assert.assertEquals(-3000, StringUtils.parseAmountEU("-3.000,00"), epsilon);


        Assert.assertEquals(3000, StringUtils.parseAmountEU(" 3,000,00"), epsilon);
        Assert.assertEquals(-3000, StringUtils.parseAmountEU(" -3,000,00"), epsilon);
        Assert.assertEquals(3000, StringUtils.parseAmountEU("d3,000,00"), epsilon);
        Assert.assertEquals(-3000, StringUtils.parseAmountEU("d-3,000,00"), epsilon);
    }
}
