package se.tink.backend.utils;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class IterableUtilsTest {

    @Test
    public void testIsPrefixOf() {
        Assert.assertTrue(IterableUtils.isPrefixOf(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(1, 2, 3, 4, 5, 6)));
        Assert.assertTrue(IterableUtils.isPrefixOf(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(1, 2, 3, 4, 5)));
        Assert.assertFalse(IterableUtils.isPrefixOf(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(2, 3)));
        Assert.assertFalse(IterableUtils.isPrefixOf(Lists.newArrayList(1, 2, 3, 4, 5, 6), Collections.singletonList(2)));

        // Differs from #sharePrefixes.
        Assert.assertFalse(IterableUtils.isPrefixOf(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPrefixWithNoBase() {
        Assert.assertFalse(IterableUtils.isPrefixOf(Lists.<Integer> newArrayList(), Lists.newArrayList(1, 2, 3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPrefixWithNoPrefix() {
        Assert.assertTrue(IterableUtils.isPrefixOf(Lists.newArrayList(1, 2, 3, 4, 5, 6), Lists.<Integer> newArrayList()));
    }

    @Test
    public void testSharePrefixes() {
        Assert.assertTrue(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(1, 2, 3, 4, 5, 6)));
        Assert.assertTrue(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(1, 2, 3, 4, 5)));
        Assert.assertFalse(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(2, 3)));
        Assert.assertFalse(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Collections.singletonList(2)));
        Assert.assertFalse(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4),
                Lists.newArrayList(1, 2, 3, 5)));

        // Differs from #isPrefixOf.
        Assert.assertTrue(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSharePrefixesWithNoBase() {
        Assert.assertFalse(IterableUtils.sharePrefixes(Lists.<Integer> newArrayList(), Lists.newArrayList(1, 2, 3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSharePrefixesWithNoPrefix() {
        Assert.assertTrue(IterableUtils.sharePrefixes(Lists.newArrayList(1, 2, 3, 4, 5, 6),
                Lists.<Integer> newArrayList()));
    }

}
