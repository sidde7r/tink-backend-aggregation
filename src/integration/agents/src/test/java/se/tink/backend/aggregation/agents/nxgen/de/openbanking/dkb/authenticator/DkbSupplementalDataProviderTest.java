package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.SupplementalStrings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.AuthResult.AuthMethod;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class DkbSupplementalDataProviderTest {

    private static final String START_CODE = "12345678";
    private static final String TAN_TEST_VALUE = "tanValue";
    private static final String PUSH_TAN_CHALLENGE = "Bitte geben Sie die pushTAN ein.";
    private static final String TEST_CHALLENGE = "Some test challenge data";
    private static final String CHALLENGE_WITH_START_CODE =
            "Sie möchten einen \"Online-Abschluss\" durchführen: Bitte bestätigen Sie den \"Startcode 12345678\" mit der Taste \"OK\".";
    private static final String TEST_SCA_METHOD_NAME = "My awesome device name";
    private static final String TEST_INDEX_VALUE = "2";
    private static final String TEST_VALUE_1 = "value1";
    private static final String TEST_VALUE_2 = "value2";

    private SupplementalInformationHelper supplementalInfoHelperMock =
            mock(SupplementalInformationHelper.class);
    private static Catalog catalog = mock(Catalog.class);

    private DkbSupplementalDataProvider tested =
            new DkbSupplementalDataProvider(supplementalInfoHelperMock, catalog);
    private List<String> challengeData;

    @BeforeClass
    public static void beforeAll() {
        when(catalog.getString(any(LocalizableKey.class)))
                .thenAnswer(i -> ((LocalizableKey) i.getArguments()[0]).get());
        when(catalog.getString(any(LocalizableParametrizedKey.class), any()))
                .thenAnswer(
                        i ->
                                MessageFormat.format(
                                        ((LocalizableParametrizedKey) i.getArguments()[0]).get(),
                                        i.getArguments()[1]));
    }

    @Test
    public void getTanCodeShouldReturnEnteredValue() throws SupplementalInfoException {
        // given
        challengeData = Collections.emptyList();
        when(supplementalInfoHelperMock.askSupplementalInformation(any()))
                .thenReturn(
                        Collections.singletonMap(
                                SupplementalStrings.GENERATED_TAN_FIELD_KEY, TAN_TEST_VALUE));

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
        Field[] result = tested.getSupplementalFields(TEST_SCA_METHOD_NAME, challengeData);

        // then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualToComparingFieldByField(getTestFieldForGeneratedTan());
    }

    @Test
    public void getFieldForGeneratedTanWhenChallengeDataDoesNotContainStartCode()
            throws SupplementalInfoException {
        // given
        challengeData = Arrays.asList(PUSH_TAN_CHALLENGE, TEST_CHALLENGE);

        // when
        Field[] result = tested.getSupplementalFields(TEST_SCA_METHOD_NAME, challengeData);

        // then
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualToComparingFieldByField(getTestFieldForGeneratedTan());
    }

    @Test
    public void getFieldForGeneratedTanWhenChallengeDataContainsStartCode()
            throws SupplementalInfoException {
        challengeData = Arrays.asList(CHALLENGE_WITH_START_CODE, TEST_CHALLENGE);

        // when
        Field[] result = tested.getSupplementalFields(TEST_SCA_METHOD_NAME, challengeData);

        // then
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualToComparingFieldByField(getTestFieldForStartcode());
        assertThat(result[1]).isEqualToComparingFieldByField(getTestFieldForGeneratedTan());
    }

    @Test
    public void selectAuthMethodShouldReturnValueSelectedByIndex()
            throws SupplementalInfoException {
        // given
        when(supplementalInfoHelperMock.askSupplementalInformation(any()))
                .thenReturn(
                        Collections.singletonMap(
                                SupplementalStrings.SELECT_AUTH_METHOD_FIELD_KEY,
                                TEST_INDEX_VALUE));

        AuthMethod givenMethod1 = new AuthMethod().setIdentifier(TEST_VALUE_1);

        AuthMethod givenMethod2 = new AuthMethod().setIdentifier(TEST_VALUE_2);

        List<AuthMethod> givenSelectionList = asList(givenMethod1, givenMethod2);

        // when
        String result = tested.selectAuthMethod(givenSelectionList);

        // then
        assertThat(result).isEqualTo(TEST_VALUE_2);
    }

    private Field getTestFieldForGeneratedTan() {
        return Field.builder()
                .name(SupplementalStrings.GENERATED_TAN_FIELD_KEY)
                .description(catalog.getString(SupplementalStrings.GENERATED_TAN_DESCRIPTION))
                .helpText(
                        catalog.getString(
                                SupplementalStrings.GENERATED_TAN_HELPTEXT_FORMAT,
                                TEST_SCA_METHOD_NAME))
                .minLength(1)
                .build();
    }

    private Field getTestFieldForStartcode() {
        return Field.builder()
                .name(SupplementalStrings.STARTCODE_FIELD_KEY)
                .description(catalog.getString(SupplementalStrings.STARTCODE_DESCRIPTION))
                .helpText(catalog.getString(SupplementalStrings.STARTCODE_HELPTEXT))
                .immutable(true)
                .value(START_CODE)
                .build();
    }
}
