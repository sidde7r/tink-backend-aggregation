package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbSupplementalDataProvider.GENERATED_TAN_KEY;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbSupplementalDataProvider.SELECT_AUTH_METHOD_KEY;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.AuthResult.AuthMethod;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class DkbSupplementalDataProviderTest {

    private SupplementalInformationHelper supplementalInfoHelperMock =
            mock(SupplementalInformationHelper.class);

    private DkbSupplementalDataProvider tested =
            new DkbSupplementalDataProvider(supplementalInfoHelperMock);

    @Test
    public void getTanCodeShouldReturnEnteredValue() throws SupplementalInfoException {
        // given
        String givenValue = "tanValue";
        when(supplementalInfoHelperMock.askSupplementalInformation(any()))
                .thenReturn(Collections.singletonMap(GENERATED_TAN_KEY, givenValue));

        // when
        String result = tested.getTanCode();

        // then
        assertThat(result).isEqualTo(givenValue);
    }

    @Test
    public void selectAuthMethodShouldReturnValueSelectedByIndex()
            throws SupplementalInfoException {
        // given
        String givenSelectedMethodIndex = "2";
        when(supplementalInfoHelperMock.askSupplementalInformation(any()))
                .thenReturn(
                        Collections.singletonMap(SELECT_AUTH_METHOD_KEY, givenSelectedMethodIndex));

        String givenValue1 = "value1";
        AuthMethod givenMethod1 = new AuthMethod().setIdentifier(givenValue1);

        String givenValue2 = "value2";
        AuthMethod givenMethod2 = new AuthMethod().setIdentifier(givenValue2);

        List<AuthMethod> givenSelectionList = asList(givenMethod1, givenMethod2);

        // when
        String result = tested.selectAuthMethod(givenSelectionList);

        // then
        assertThat(result).isEqualTo(givenValue2);
    }
}
