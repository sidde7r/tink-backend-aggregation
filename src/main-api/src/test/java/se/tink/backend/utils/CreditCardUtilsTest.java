package se.tink.backend.utils;

import org.junit.Assert;
import org.junit.Test;

public class CreditCardUtilsTest {

    @Test
    public void testMaskLongNumbers() {
        Assert.assertEquals(CreditCardUtils.maskCardNumber("1234123412341234"), "**** **** **** 1234");
        Assert.assertEquals(CreditCardUtils.maskCardNumber("12341234121234"), "**** **** ** 1234");
        Assert.assertEquals(CreditCardUtils.maskCardNumber("123412341234"), "**** **** 1234");
        Assert.assertEquals(CreditCardUtils.maskCardNumber("121234"), "** 1234");
    }

    @Test
    public void testMaskShortNumbers() {
        Assert.assertEquals(CreditCardUtils.maskCardNumber("1234"), "1234");
        Assert.assertEquals(CreditCardUtils.maskCardNumber("1"), "1");
        Assert.assertEquals(CreditCardUtils.maskCardNumber(""), "");
    }

    @Test
    public void testMaskWithDelimiters() {
        Assert.assertEquals(CreditCardUtils.maskCardNumber("1234-1234-1234-1234"), "**** **** **** 1234");
        Assert.assertEquals(CreditCardUtils.maskCardNumber("1234 1234 1234 1234"), "**** **** **** 1234");
        Assert.assertEquals(CreditCardUtils.maskCardNumber("1234.1234.1234.1234"), "**** **** **** 1234");
    }

    @Test
    public void testMaskWrongCharacters() {
        Assert.assertEquals(CreditCardUtils.maskCardNumber("abcde"), "");
    }

}