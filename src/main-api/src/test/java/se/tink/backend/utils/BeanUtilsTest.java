package se.tink.backend.utils;

import org.junit.Assert;
import org.junit.Test;

import se.tink.backend.core.Modifiable;
import se.tink.backend.core.Transaction;

public class BeanUtilsTest {
    @Test
    public void testCopyProperties() {
        Transaction t1 = new Transaction();
        t1.setDescription("test1");

        Transaction t2 = new Transaction();
        t2.setDescription("test2");

        Assert.assertNotEquals(t1.getDescription(), t2.getDescription());
        
        BeanUtils.copyProperties(t1, t2, Modifiable.class);

        Assert.assertEquals(t1.getDescription(), t2.getDescription());
    }
}
