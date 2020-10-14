package se.tink.backend.aggregation.agents.utils.supplementalfields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class GermanFieldsTest {

    private static final String STARTCODE = "111999222";
    private static final String SCA_METHOD_NAME = "phoneName001";
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
    public void shouldReturnProperTanFieldWithoutScaMethodName() {
        // given

        // when
        Field result = GermanFields.Tan.build(catalog, null);

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
    public void shouldReturnProperTanFieldWithScaMethodName() {
        // given

        // when
        Field result = GermanFields.Tan.build(catalog, SCA_METHOD_NAME);

        // then
        assertThat(result.getName()).isEqualTo("tanField");
        assertThat(result.getDescription()).isEqualTo("TAN");
        assertThat(result.getValue()).isNull();
        assertThat(result.getHint()).isNull();
        assertThat(result.getHelpText())
                .isEqualTo(
                        "Confirm by entering the generated TAN for \"" + SCA_METHOD_NAME + "\".");
        assertThat(result.isNumeric()).isFalse();
        assertThat(result.getMinLength()).isEqualTo(1);
        assertThat(result.getMaxLength()).isNull();
        assertThat(result.isImmutable()).isFalse();
        assertThat(result.getPattern()).isNull();
        assertThat(result.getPatternError()).isNull();

        verify(catalog).getString(any(LocalizableKey.class));
        verify(catalog).getString(any(LocalizableParametrizedKey.class), anyString());
        verifyNoMoreInteractions(catalog);
    }
}
