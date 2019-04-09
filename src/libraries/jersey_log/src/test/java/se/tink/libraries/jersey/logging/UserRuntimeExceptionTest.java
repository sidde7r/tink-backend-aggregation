package se.tink.libraries.jersey.logging;

import org.junit.Assert;
import org.junit.Test;

public class UserRuntimeExceptionTest {

    private static final String DEMO_USER_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Test
    public void testBasicConstruction() throws Exception {
        Assert.assertEquals(
                String.format("[userId:%s] Something went wrong.", DEMO_USER_ID),
                new UserRuntimeException(DEMO_USER_ID, new RuntimeException()).getMessage());
        Assert.assertEquals(
                String.format("[userId:%s] My error message.", DEMO_USER_ID),
                new UserRuntimeException(DEMO_USER_ID, "My error message.", new RuntimeException())
                        .getMessage());
    }

    @Test
    public void testNullUser() throws Exception {
        // Making sure we don't throw NPE or something.
        Assert.assertEquals(
                "[userId:null] Something went wrong.",
                new UserRuntimeException(null, new RuntimeException()).getMessage());
        Assert.assertEquals(
                "[userId:null] My error message.",
                new UserRuntimeException(null, "My error message.", new RuntimeException())
                        .getMessage());
    }
}
