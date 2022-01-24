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
public class MitIdCprFieldTest {

    @Test
    @Parameters(method = "paramsForValidateCpr")
    public void should_validate_cpr(String cpr, boolean isValid) {
        // when
        Throwable throwable = catchThrowable(() -> MitIdCprField.assertValidCpr(cpr));

        // then
        if (isValid) {
            assertThat(throwable).isNull();
        } else {
            assertThat(throwable)
                    .isInstanceOf(MitIdError.INVALID_CPR_FORMAT.exception().getClass());
        }
    }

    @SuppressWarnings("unused")
    private static Object[] paramsForValidateCpr() {
        return new Object[] {
            asArray(repeat("1", 9), false),
            asArray(repeat("1", 10), true),
            asArray(repeat("1", 11), false),
            asArray(repeat("a", 10), false),
            asArray("123456789a", false)
        };
    }

    private static Object[] asArray(Object... objects) {
        return objects;
    }
}
