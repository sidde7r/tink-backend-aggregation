package se.tink.backend.utils;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RotatedListViewTest {

    private ImmutableList<Integer> delegate;

    @Before
    public void setUp() {
        this.delegate = ImmutableList.of(1, 2, 3);
    }

    @Test
    public void testPositiveRotation() {
        Assert.assertEquals(ImmutableList.of(3, 1, 2), ImmutableList.copyOf(RotatedListView.of(delegate, 1)));
    }

    @Test
    public void testNegativeRotation() {
        Assert.assertEquals(ImmutableList.of(2, 3, 1), ImmutableList.copyOf(RotatedListView.of(delegate, -1)));
    }

}
