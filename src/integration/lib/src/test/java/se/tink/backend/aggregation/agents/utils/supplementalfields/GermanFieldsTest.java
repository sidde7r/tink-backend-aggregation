package se.tink.backend.aggregation.agents.utils.supplementalfields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.OtpFormat;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.i18n_aggregation.LocalizableParametrizedKey;

@RunWith(JUnitParamsRunner.class)
public class GermanFieldsTest {

    private static final String STARTCODE = "111999222";
    private static final String SCA_METHOD_NAME = "phoneName001";
    private static final String SMS_TAN_FIELD_NAME = "smsTan";

    private Catalog catalog;

    @Before
    public void before() {
        catalog = mock(Catalog.class);
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
    public void shouldReturnProperStartcodeField() {
        // given

        // when
        Field result = GermanFields.Startcode.build(catalog, STARTCODE);

        // then
        assertThat(result.getName()).isEqualTo("startcodeField");
        assertThat(result.getDescription()).isEqualTo("Startcode");
        assertThat(result.getValue()).isEqualTo(STARTCODE);
        assertThat(result.getHint()).isNull();
        assertThat(result.getHelpText())
                .isEqualTo(
                        "Insert your girocard into the TAN-generator and press \"TAN\". Enter the startcode and press \"OK\".");
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMinLength()).isNull();
        assertThat(result.getMaxLength()).isNull();
        assertThat(result.isImmutable()).isTrue();
        assertThat(result.getPattern()).isNull();
        assertThat(result.getPatternError()).isNull();

        verify(catalog, times(2)).getString(any(LocalizableKey.class));
        verifyNoMoreInteractions(catalog);
    }

    @Test
    public void shouldReturnProperTanFieldWithoutDetails() {
        // when
        Field result = GermanFields.Tan.builder(catalog).build();

        // then
        assertThat(result.getName()).isEqualTo("tanField");
        assertThat(result.getDescription()).isEqualTo("TAN");
        assertThat(result.getValue()).isNull();
        assertThat(result.getHint()).isNull();
        assertThat(result.getHelpText()).isEqualTo("Confirm by entering the generated TAN.");
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMinLength()).isEqualTo(1);
        assertThat(result.getMaxLength()).isNull();
        assertThat(result.isImmutable()).isFalse();
        assertThat(result.getPattern()).isNull();
        assertThat(result.getPatternError()).isNull();

        verify(catalog, times(2)).getString(any(LocalizableKey.class));
        verifyNoMoreInteractions(catalog);
    }

    @Test
    public void shouldReturnProperTanFieldWithoutSomeDetails() {
        // when
        Field result = GermanFields.Tan.builder(catalog).otpMaxLength(10).build();

        // then
        assertThat(result.getName()).isEqualTo("tanField");
        assertThat(result.getDescription()).isEqualTo("TAN");
        assertThat(result.getValue()).isNull();
        assertThat(result.getHint()).isEqualTo("_ _ _ _ _ _ _ _ _ _");
        assertThat(result.getHelpText()).isEqualTo("Confirm by entering the generated TAN.");
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMinLength()).isEqualTo(1);
        assertThat(result.getMaxLength()).isEqualTo(10);
        assertThat(result.isImmutable()).isFalse();
        assertThat(result.getPattern()).isNull();
        assertThat(result.getPatternError()).isNull();

        verify(catalog, times(2)).getString(any(LocalizableKey.class));
        verifyNoMoreInteractions(catalog);
    }

    @Test
    public void shouldReturnProperTanFieldWithScaMethodNameAndKnownOTP() {
        // given
        String authenticationType = "SMS_OTP";

        // when
        Field result =
                GermanFields.Tan.builder(catalog)
                        .authenticationType(authenticationType)
                        .authenticationMethodName(SCA_METHOD_NAME)
                        .build();

        // then
        verifyCommonOtpProperties(result);

        assertThat(result.getName()).isEqualTo(SMS_TAN_FIELD_NAME);
        assertThat(result.getHint()).isNull();
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMaxLength()).isNull();
        assertThat(result.getPattern()).isNull();
        assertThat(result.getPatternError()).isNull();

        verify(catalog).getString(any(LocalizableKey.class));
        verify(catalog).getString(any(LocalizableParametrizedKey.class), anyString());
        verifyNoMoreInteractions(catalog);
    }

    @Test
    public void shouldReturnProperTanFieldForNumericOtp() {
        // given
        Integer otpMaxLength = 5;

        // when
        Field result =
                GermanFields.Tan.builder(catalog)
                        .authenticationType("SMS_OTP")
                        .authenticationMethodName(SCA_METHOD_NAME)
                        .otpFormat(OtpFormat.INTEGER)
                        .otpMaxLength(otpMaxLength)
                        .build();

        // then
        verifyCommonOtpProperties(result);

        assertThat(result.getName()).isEqualTo(SMS_TAN_FIELD_NAME);
        assertThat(result.getHint()).isEqualTo("_ _ _ _ _");
        assertThat(result.isNumeric()).isTrue();
        assertThat(result.getMaxLength()).isEqualTo(otpMaxLength);
        assertThat(result.getPattern()).isEqualTo("^[0-9]{1," + otpMaxLength + "}$");
        assertThat(result.getPatternError())
                .isEqualTo("Please enter a maximum of " + otpMaxLength + " digits");

        verify(catalog).getString(any(LocalizableKey.class));
        verify(catalog).getString(any(LocalizableParametrizedKey.class), anyString());
        verify(catalog).getString(any(LocalizableParametrizedKey.class), eq(otpMaxLength));
        verifyNoMoreInteractions(catalog);
    }

    @Test
    public void shouldReturnProperTanFieldForCharactersOtp() {
        // given
        final Integer otpMaxLength = 7;

        // when
        Field result =
                GermanFields.Tan.builder(catalog)
                        .authenticationType("SMS_OTP")
                        .authenticationMethodName(SCA_METHOD_NAME)
                        .otpFormat(OtpFormat.CHARACTERS)
                        .otpMinLength(1)
                        .otpMaxLength(otpMaxLength)
                        .build();

        // then
        verifyCommonOtpProperties(result);

        assertThat(result.getName()).isEqualTo(SMS_TAN_FIELD_NAME);
        assertThat(result.getHint()).isEqualTo("_ _ _ _ _ _ _");
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMaxLength()).isEqualTo(otpMaxLength);
        assertThat(result.getPattern()).isEqualTo("^[^\\s]{1," + otpMaxLength + "}$");
        assertThat(result.getPatternError())
                .isEqualTo("Please enter a maximum of " + otpMaxLength + " characters");

        verify(catalog).getString(any(LocalizableKey.class));
        verify(catalog).getString(any(LocalizableParametrizedKey.class), anyString());
        verify(catalog).getString(any(LocalizableParametrizedKey.class), eq(otpMaxLength));
        verifyNoMoreInteractions(catalog);
    }

    @Test
    @Parameters({
        "SMS_OTP, smsTan",
        "CHIP_OTP, chipTan",
        "PHOTO_OTP, photoTan",
        "PUSH_OTP, pushTan",
        "SMTP_OTP, smtpTan",
        "UNEXPECTED, tanField"
    })
    public void shouldSetProperFieldNameDependingOnAuthType(
            String authenticationType, String expectedFieldName) {
        // given

        // when
        Field result =
                GermanFields.Tan.builder(catalog).authenticationType(authenticationType).build();

        // then
        assertThat(result.getName()).isEqualTo(expectedFieldName);
        assertThat(result.getDescription()).isEqualTo("TAN");
        assertThat(result.getValue()).isNull();
        assertThat(result.getHint()).isNull();
        assertThat(result.getHelpText()).isEqualTo("Confirm by entering the generated TAN.");
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMinLength()).isEqualTo(1);
        assertThat(result.getMaxLength()).isNull();
        assertThat(result.isImmutable()).isFalse();
        assertThat(result.getPattern()).isNull();
        assertThat(result.getPatternError()).isNull();
    }

    private void verifyCommonOtpProperties(Field result) {
        assertThat(result.getDescription()).isEqualTo("TAN");
        assertThat(result.getValue()).isNull();
        assertThat(result.getHelpText())
                .isEqualTo(
                        "Confirm by entering the generated TAN for \"" + SCA_METHOD_NAME + "\".");
        assertThat(result.getMinLength()).isEqualTo(1);
        assertThat(result.isImmutable()).isFalse();
    }

    @Test
    public void shouldReturnProperSelectOptions() {
        // given
        GermanFields.SelectEligible mockSelectEligible =
                getSelectEligible("PUSH_TAN", "Push", "https://test_url.com/push");
        GermanFields.SelectEligible mockSelectEligible2 =
                getSelectEligible("SMS_TAN", "SMS", "https://test_url.com/sms");

        List<GermanFields.SelectEligible> selectEligibles =
                Arrays.asList(mockSelectEligible, mockSelectEligible2);

        // when
        List<SelectOption> selectOptions =
                GermanFields.SelectOptions.prepareSelectOptions(selectEligibles);

        // then
        assertThat(selectOptions).hasSize(2);
        SelectOption resultSelectOption = selectOptions.get(0);
        assertThat(resultSelectOption.getValue()).isEqualTo("1");
        assertThat(resultSelectOption.getText()).isEqualTo("Push");
        assertThat(resultSelectOption.getIconUrl()).isEqualTo("https://test_url.com/push");
        SelectOption resultSelectOption2 = selectOptions.get(1);
        assertThat(resultSelectOption2.getValue()).isEqualTo("2");
        assertThat(resultSelectOption2.getText()).isEqualTo("SMS");
        assertThat(resultSelectOption2.getIconUrl()).isEqualTo("https://test_url.com/sms");
    }

    private GermanFields.SelectEligible getSelectEligible(
            String authenticationType, String name, String url) {
        GermanFields.SelectEligible mockSelectEligible = mock(GermanFields.SelectEligible.class);
        when(mockSelectEligible.getAuthenticationType()).thenReturn(authenticationType);
        when(mockSelectEligible.getName()).thenReturn(name);
        when(mockSelectEligible.getIconUrl()).thenReturn(url);
        return mockSelectEligible;
    }
}
