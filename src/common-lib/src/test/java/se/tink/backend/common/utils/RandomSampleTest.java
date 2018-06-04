package se.tink.backend.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.tink.backend.common.utils.RandomSample;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RandomSampleTest {
    private ArrayList<Integer> duplicateList = Lists.newArrayList(1, 1, 1, 1, 1);
    private RandomSample<Integer> duplicateSampler;
    private final ArrayList<Integer> uniqueList = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private RandomSample<Integer> uniqueSampler;

    @Before
    public void setUp() {
        // Expecting all elements to be unique
        Assert.assertEquals(uniqueList.size(), Sets.newHashSet(uniqueList).size());

        int magicSeed = 42;
        Random random = new Random(magicSeed);

        uniqueSampler = RandomSample.from(uniqueList, random);
        duplicateSampler = RandomSample.from(duplicateList, random);
    }

    @Test
    public void testAllSample() {
        List<Integer> sample = uniqueSampler.pick(10);
        Assert.assertEquals(10, sample.size());
        Assert.assertEquals(Sets.newHashSet(sample), Sets.newHashSet(uniqueList));
    }

    @Test
    public void testEmptySample() {
        List<Integer> sample = uniqueSampler.pick(0);
        Assert.assertEquals(0, sample.size());
    }

    @Test
    public void testMajoritySample() {
        List<Integer> sample = uniqueSampler.pick(8);
        Assert.assertEquals(8, sample.size());
        Assert.assertTrue(Sets.newHashSet(uniqueList).containsAll(sample));
    }

    @Test
    public void testMinoritySample() {
        List<Integer> sample = uniqueSampler.pick(3);
        Assert.assertEquals(3, sample.size());
        Assert.assertTrue(Sets.newHashSet(uniqueList).containsAll(sample));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverSample() {
        uniqueSampler.pick(11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testunderSample() {
        uniqueSampler.pick(-1);
    }

    @Test
    public void voidTestPickingDuplicates() {
        List<Integer> sample = duplicateSampler.pick(5);
        Assert.assertEquals(5, sample.size());
        Assert.assertEquals(sample, duplicateList);
    }
}
