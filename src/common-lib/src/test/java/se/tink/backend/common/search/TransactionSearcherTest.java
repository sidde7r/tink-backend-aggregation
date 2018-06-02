package se.tink.backend.common.search;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

public class TransactionSearcherTest {

    @Test
    public void testTrimmingSplitter() {
        Splitter queryStringSplitter = Splitter.on(" ").trimResults().omitEmptyStrings();
        ImmutableList<String> pieces = ImmutableList.copyOf(queryStringSplitter.split(" hej "));
        Assert.assertEquals(1, pieces.size());
        Assert.assertEquals("hej", pieces.get(0));
    }

}
