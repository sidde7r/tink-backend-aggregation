package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.LoginStep.getPasswordStringAsIntegerList;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.LoginStep.getUsernameType;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.UsernameTypes;

public class LoginStepTest {

    private static final List VALID_PINPAD = Arrays.asList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0);

    @Test
    public void testGetPinPositionsForPassword() throws LoginException {

        List<Integer> pinPositions =
                LoginStep.getPinPositionsForPassword(
                        getPasswordStringAsIntegerList("123456"),
                        VALID_PINPAD,
                        Arrays.asList(1, 3, 5));

        assertEquals(3, pinPositions.size());

        assertArrayEquals(
                Arrays.asList(8, 6, 4).toArray(new Integer[0]),
                pinPositions.toArray(new Integer[0]));
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfAlphaNumericPassword() throws LoginException {
        getPasswordStringAsIntegerList("12345A");
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfPinpadNumbersMissing() throws LoginException {

        LoginStep.getPinPositionsForPassword(
                getPasswordStringAsIntegerList("123456"),
                Arrays.asList(9, 8, 7, 2, 1, 0),
                Arrays.asList(1, 3, 5));
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfInvalidPinPositionRequested() throws LoginException {
        LoginStep.getPinPositionsForPassword(
                getPasswordStringAsIntegerList("123456"), VALID_PINPAD, Arrays.asList(1, 3, 7));
    }

    @Test(expected = LoginException.class)
    public void testExceptionThrownIfZeroPinPositionRequested() throws LoginException {
        LoginStep.getPinPositionsForPassword(
                getPasswordStringAsIntegerList("123456"), VALID_PINPAD, Arrays.asList(0, 3, 6));
    }

    @Test
    public void testUsernameTypes() throws LoginException {
        assertEquals(UsernameTypes.NIF, getUsernameType("12345678Z"));
        assertEquals(UsernameTypes.NIF, getUsernameType("12345677J"));
        assertEquals(UsernameTypes.NIE, getUsernameType("Z2345678M"));
        assertEquals(UsernameTypes.NIE, getUsernameType("X2345677E"));
        assertEquals(UsernameTypes.PASSPORT, getUsernameType("XAB123456"));
        assertEquals(UsernameTypes.PASSPORT, getUsernameType("AB123456"));
        assertEquals(UsernameTypes.PASSPORT, getUsernameType("12345678A"));
    }
}
