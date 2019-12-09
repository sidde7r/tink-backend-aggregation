package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.utils;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public class RangeRegexUtilsTest {

    @Test
    public void testFillByNines() {
        Assert.assertEquals(19, RangeRegexUtils.fillByNines(17, 1));
        Assert.assertEquals(99, RangeRegexUtils.fillByNines(17, 2));
        Assert.assertEquals(999, RangeRegexUtils.fillByNines(17, 3));
    }

    @Test
    public void testFillByZeros() {
        Assert.assertEquals(190, RangeRegexUtils.fillByZeros(199, 1));
        Assert.assertEquals(100, RangeRegexUtils.fillByZeros(199, 2));
        Assert.assertEquals(0, RangeRegexUtils.fillByZeros(199, 3));
        Assert.assertEquals(0, RangeRegexUtils.fillByZeros(199, 4));
    }

    @Test
    public void testSplitToRanges() {
        Assert.assertEquals(Arrays.asList(19, 25), RangeRegexUtils.splitToRanges(17, 25));
        Assert.assertEquals(
                Arrays.asList(19, 99, 199, 249, 255), RangeRegexUtils.splitToRanges(17, 255));
    }

    @Test
    public void testRangeToPattern() {
        Assert.assertEquals("1[7-9]", RangeRegexUtils.rangeToPattern(17, 19));
        Assert.assertEquals("17", RangeRegexUtils.rangeToPattern(17, 17));
        Assert.assertEquals("[2-9][0-9]", RangeRegexUtils.rangeToPattern(20, 99));
        Assert.assertEquals("2[0-9]{2}", RangeRegexUtils.rangeToPattern(200, 299));
        Assert.assertEquals("2[0-9]{2}", RangeRegexUtils.rangeToPattern(200, 2999));
    }

    @Test
    public void testSplitToPatterns() {
        Assert.assertEquals(
                Arrays.asList("1[7-9]", "2[0-5]"), RangeRegexUtils.splitToPatterns(17, 25));
        Assert.assertEquals(
                Arrays.asList("1[7-9]", "[2-9][0-9]", "[1-2][0-9]{2}", "300"),
                RangeRegexUtils.splitToPatterns(17, 300));
    }

    @Test
    public void testRegexForRange() {
        Assert.assertEquals("^(1[7-9]|2[0-5])$", RangeRegexUtils.regexForRange(17, 25));
        Assert.assertEquals(
                "^(1[7-9]|[2-9][0-9]|[1-2][0-9]{2}|300)$", RangeRegexUtils.regexForRange(17, 300));
        Assert.assertEquals(
                "^(-[1-9]|-1[0-7]|[0-9]|[1-9][0-9]|[1-2][0-9]{2}|300)$",
                RangeRegexUtils.regexForRange(-17, 300));
    }

    @Test
    public void testPatternMatch() {
        Assert.assertTrue(
                Pattern.compile(RangeRegexUtils.regexForRange(17, 300)).matcher("25").find());
        Assert.assertTrue(
                Pattern.compile(RangeRegexUtils.regexForRange(17, 300)).matcher("255").find());
        Assert.assertTrue(
                Pattern.compile(RangeRegexUtils.regexForRange(-17, 300)).matcher("-10").find());
        Assert.assertFalse(
                Pattern.compile(RangeRegexUtils.regexForRange(17, 300)).matcher("15").find());
        Assert.assertFalse(
                Pattern.compile(RangeRegexUtils.regexForRange(17, 300)).matcher("305").find());
        Assert.assertFalse(
                Pattern.compile(RangeRegexUtils.regexForRange(-17, 300)).matcher("-18").find());
    }
}
