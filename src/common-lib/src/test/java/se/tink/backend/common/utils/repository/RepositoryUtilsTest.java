package se.tink.backend.common.utils.repository;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import se.tink.backend.common.utils.repository.RepositoryUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RepositoryUtilsTest {

    @Test
    public void testHexPrefixGeneration() {
        final ArrayList<String> testList = Lists.newArrayList(RepositoryUtils.hexPrefixes(2));
        Assert.assertEquals(256, testList.size());
        
        final HashSet<String> testSet = Sets.newHashSet(testList);
        Assert.assertEquals(256, testSet.size());
        Assert.assertTrue(testSet.contains("00"));
        Assert.assertTrue(testSet.contains("01"));
        Assert.assertTrue(testSet.contains("02"));
        Assert.assertTrue(testSet.contains("ef"));
        Assert.assertTrue(testSet.contains("ff"));
    }

}
