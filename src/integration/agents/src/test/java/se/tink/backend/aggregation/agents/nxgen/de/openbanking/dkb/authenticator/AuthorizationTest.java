package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.Authorization.ScaMethod;

public class AuthorizationTest {

    @Test
    public void isScaMethodSelectionRequiredShouldReturnTrueForNotEmptyMethodsSet() {
        // given
        Authorization tested = new Authorization();
        tested.setScaMethods(singletonList(new ScaMethod()));

        // when
        boolean result = tested.isScaMethodSelectionRequired();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isScaMethodSelectionRequiredShouldReturnFalseTrueForEmptyMethodsSet() {
        // given
        Authorization tested = new Authorization();

        // when
        boolean result = tested.isScaMethodSelectionRequired();

        // then
        assertThat(result).isFalse();
    }
}
