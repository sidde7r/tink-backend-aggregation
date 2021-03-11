package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ScaExpirationValidatorTest {

    private static ScaExpirationValidator validator;
    private static PersistentStorage storage;

    @Before
    public void setUp() throws Exception {
        storage = mock(PersistentStorage.class);
        long limitInMinutes = 5;
        validator = new ScaExpirationValidator(storage, limitInMinutes);
    }

    @Test
    public void shouldReturnFalseIfScaNotExpired() {
        // given
        given(storage.get(ScaExpirationValidator.LAST_SCA_TIME, String.class))
                .willReturn(Optional.of(LocalDateTime.now().minusMinutes(2).toString()));

        // expected
        Assertions.assertThat(validator.isScaExpired()).isFalse();
    }

    @Test
    public void shouldReturnTrueIfScaExpired() {
        // given
        given(storage.get(ScaExpirationValidator.LAST_SCA_TIME, String.class))
                .willReturn(Optional.of("2021-01-01T00:00:01.000"));

        // expected
        Assertions.assertThat(validator.isScaExpired()).isTrue();
    }
}
