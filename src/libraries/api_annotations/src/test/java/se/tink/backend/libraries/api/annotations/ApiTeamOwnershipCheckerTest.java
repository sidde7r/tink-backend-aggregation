package se.tink.backend.libraries.api.annotations;

import org.junit.Test;

public class ApiTeamOwnershipCheckerTest {

    @Test(expected = AssertionError.class)
    public void testFailingTest() {
        new ApiTeamOwnershipChecker("se.tink.api.annotations.failing").check();
    }

    @Test
    public void testPassing() {
        new ApiTeamOwnershipChecker("se.tink.api.annotations.passing").check();
    }

    @Test(expected = AssertionError.class)
    public void testEmptyPackage() {
        new ApiTeamOwnershipChecker("se.tink.api.annotations.missing").check();
    }
}
