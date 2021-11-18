package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAccountSegment;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LaBanquePostaleUserStateTest {
    private PersistentStorage persistentStorage;
    private LaBanquePostaleUserState userState;

    @Before
    public void setUp() {
        persistentStorage = new PersistentStorage();
        userState = new LaBanquePostaleUserState(persistentStorage);
    }

    @Test
    public void shouldReturnTrueForCheckingIfAccountSegmentIsSetToBusiness() {
        // when
        userState.specifyAccountSegment(LaBanquePostaleAccountSegment.BUSINESS);

        // then
        Assertions.assertThat(
                        persistentStorage.get(
                                "ACCOUNT_SEGMENT", LaBanquePostaleAccountSegment.class))
                .contains(LaBanquePostaleAccountSegment.BUSINESS);
        Assertions.assertThat(userState.isBusinessAccountSegment()).isTrue();
    }

    @Test
    public void shouldReturnFalseForCheckingIfAccountSegmentIsSetToPersonal() {
        // when
        userState.specifyAccountSegment(LaBanquePostaleAccountSegment.PERSONAL);

        // then
        Assertions.assertThat(
                        persistentStorage.get(
                                "ACCOUNT_SEGMENT", LaBanquePostaleAccountSegment.class))
                .contains(LaBanquePostaleAccountSegment.PERSONAL);
        Assertions.assertThat(userState.isBusinessAccountSegment()).isFalse();
    }

    @Test
    public void shouldReturnFalseForCheckingIfAccountSegmentEmpty() {
        // when then
        Assertions.assertThat(
                        persistentStorage
                                .get("ACCOUNT_SEGMENT", LaBanquePostaleAccountSegment.class)
                                .isPresent())
                .isFalse();
        Assertions.assertThat(userState.isBusinessAccountSegment()).isFalse();
    }
}
