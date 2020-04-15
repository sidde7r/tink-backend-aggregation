package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.ConsentAuthorization.ScaMethod;

public class ConsentAuthorizationTest {

    @Test
    public void isScaMethodSelectionRequiredShouldReturnTrueForNotEmptyMethodsSet() {
        // given
        ConsentAuthorization tested = new ConsentAuthorization();
        tested.setScaMethods(singletonList(new ScaMethod()));

        // when
        boolean result = tested.isScaMethodSelectionRequired();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isScaMethodSelectionRequiredShouldReturnFalseTrueForEmptyMethodsSet() {
        // given
        ConsentAuthorization tested = new ConsentAuthorization();

        // when
        boolean result = tested.isScaMethodSelectionRequired();

        // then
        assertThat(result).isFalse();
    }
}
