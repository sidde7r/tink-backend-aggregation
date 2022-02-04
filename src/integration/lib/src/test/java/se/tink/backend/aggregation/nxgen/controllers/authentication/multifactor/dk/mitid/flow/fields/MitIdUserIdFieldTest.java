package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;

@RunWith(JUnitParamsRunner.class)
public class MitIdUserIdFieldTest {

    @Test
    @Parameters(method = "paramsForValidateUserId")
    public void should_validate_user_id(String userId, boolean isValid) {
        // when
        Throwable throwable = catchThrowable(() -> MitIdUserIdField.assertValidUserId(userId));

        // then
        if (isValid) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable)
                    .isInstanceOf(MitIdError.INVALID_CPR_FORMAT.exception().getClass());
        }
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForValidateUserId() {
        return new Object[] {
            asArray(repeat("a", 4), false),
            asArray(repeat("a", 5), true),
            asArray(repeat("a", 48), true),
            asArray(repeat("a", 49), false),
            asArray("abcdefghijklmnopqrstuvwxyzæøå", true),
            asArray("0123456789{}!#$^,*()_+-=:;?.@ ", true),
            asArray("ęśąćż", false)
        };
    }

    private static Object[] asArray(Object... objects) {
        return objects;
    }
}
