package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAccountSegment;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsUserStateTest {

    private PersistentStorage persistentStorage;

    private SibsUserState objectUnderTest;

    @Before
    public void init() {
        persistentStorage = new PersistentStorage();
        objectUnderTest = new SibsUserState(persistentStorage);
    }

    @Test
    public void shouldReturnTrueForCheckingIfAccountSegmentIsSetToBusiness() {
        // given
        objectUnderTest.specifyAccountSegment(SibsAccountSegment.BUSINESS);

        // when
        objectUnderTest.isBusinessAccountSegment();

        // then
        Assertions.assertThat(persistentStorage.get("ACCOUNT_SEGMENT", SibsAccountSegment.class))
                .contains(SibsAccountSegment.BUSINESS);
        Assertions.assertThat(objectUnderTest.isBusinessAccountSegment()).isTrue();
    }

    @Test
    public void shouldReturnFalseForCheckingIfAccountSegmentIsSetToPersonal() {
        // given
        objectUnderTest.specifyAccountSegment(SibsAccountSegment.PERSONAL);

        // when
        objectUnderTest.isBusinessAccountSegment();

        // then
        Assertions.assertThat(persistentStorage.get("ACCOUNT_SEGMENT", SibsAccountSegment.class))
                .contains(SibsAccountSegment.PERSONAL);
        Assertions.assertThat(objectUnderTest.isBusinessAccountSegment()).isFalse();
    }

    @Test
    public void shouldReturnFalseForCheckingIfAccountSegmentEmpty() {
        // when
        objectUnderTest.isBusinessAccountSegment();

        // then
        Assertions.assertThat(
                        persistentStorage
                                .get("ACCOUNT_SEGMENT", SibsAccountSegment.class)
                                .isPresent())
                .isFalse();
        Assertions.assertThat(objectUnderTest.isBusinessAccountSegment()).isFalse();
    }
}
