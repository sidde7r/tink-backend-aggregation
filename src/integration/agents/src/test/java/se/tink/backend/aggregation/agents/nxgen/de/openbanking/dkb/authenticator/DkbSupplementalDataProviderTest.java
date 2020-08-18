package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.SupplementalDataKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.SupplementalDataLabels;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.AuthResult.AuthMethod;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class DkbSupplementalDataProviderTest {

    private static final String START_CODE = "12345678";
    private static final String TAN_TEST_VALUE = "tanValue";
    private static final String PUSH_TAN_CHALLENGE = "Bitte geben Sie die pushTAN ein.";
    private static final String TEST_CHALLENGE = "Some test challenge data";
    private static final String CHALLENGE_WITH_START_CODE =
            "Sie möchten einen \"Online-Abschluss\" durchführen: Bitte bestätigen Sie den \"Startcode 12345678\" mit der Taste \"OK\".";
    private static final String TEST_INDEX_VALUE = "2";
    private static final String TEST_VALUE_1 = "value1";
    private static final String TEST_VALUE_2 = "value2";

    private SupplementalInformationHelper supplementalInfoHelperMock =
            mock(SupplementalInformationHelper.class);

    private DkbSupplementalDataProvider tested =
            new DkbSupplementalDataProvider(supplementalInfoHelperMock);
    private List<String> challengeData;

    @Test
    public void getTanCodeShouldReturnEnteredValue() throws SupplementalInfoException {
        // given
        challengeData = Collections.emptyList();
        when(supplementalInfoHelperMock.askSupplementalInformation(any()))
                .thenReturn(
                        Collections.singletonMap(
                                SupplementalDataKeys.GENERATED_TAN_KEY, TAN_TEST_VALUE));

        // when
        String result = tested.getTanCode(challengeData);

        // then
        assertThat(result).isEqualTo(TAN_TEST_VALUE);
    }

    @Test
    public void getFieldForGeneratedTanWhenChallengeDataIsEmptyList()
            throws SupplementalInfoException {
        // given
        challengeData = Collections.emptyList();

        // when
        Field result = tested.getFieldForGeneratedTan(challengeData);

        // then
        assertThat(result)
                .isEqualToComparingFieldByField(
                        getTestFieldForGeneratedTan(SupplementalDataLabels.GENERATED_TAN_LABEL));
    }

    @Test
    public void getFieldForGeneratedTanWhenChallengeDataDoesNotContainStartCode()
            throws SupplementalInfoException {
        // given
        challengeData = Arrays.asList(PUSH_TAN_CHALLENGE, TEST_CHALLENGE);

        // when
        Field result = tested.getFieldForGeneratedTan(challengeData);

        // then
        assertThat(result)
                .isEqualToComparingFieldByField(
                        getTestFieldForGeneratedTan(SupplementalDataLabels.GENERATED_TAN_LABEL));
    }

    @Test
    public void getFieldForGeneratedTanWhenChallengeDataContainsStartCode()
            throws SupplementalInfoException {
        challengeData = Arrays.asList(CHALLENGE_WITH_START_CODE, TEST_CHALLENGE);

        // when
        Field result = tested.getFieldForGeneratedTan(challengeData);

        // then
        assertThat(result)
                .isEqualToComparingFieldByField(
                        getTestFieldForGeneratedTan(
                                String.format(
                                        SupplementalDataLabels.CHIP_TAN_DESCRIPTION_LABEL,
                                        START_CODE)));
    }

    @Test
    public void selectAuthMethodShouldReturnValueSelectedByIndex()
            throws SupplementalInfoException {
        // given
        when(supplementalInfoHelperMock.askSupplementalInformation(any()))
                .thenReturn(
                        Collections.singletonMap(
                                SupplementalDataKeys.SELECT_AUTH_METHOD_KEY, TEST_INDEX_VALUE));

        AuthMethod givenMethod1 = new AuthMethod().setIdentifier(TEST_VALUE_1);

        AuthMethod givenMethod2 = new AuthMethod().setIdentifier(TEST_VALUE_2);

        List<AuthMethod> givenSelectionList = asList(givenMethod1, givenMethod2);

        // when
        String result = tested.selectAuthMethod(givenSelectionList);

        // then
        assertThat(result).isEqualTo(TEST_VALUE_2);
    }

    private Field getTestFieldForGeneratedTan(String description) {
        return Field.builder()
                .description(description)
                .name(SupplementalDataKeys.GENERATED_TAN_KEY)
                .numeric(false)
                .minLength(1)
                .maxLength(MAX_VALUE)
                .build();
    }
}
