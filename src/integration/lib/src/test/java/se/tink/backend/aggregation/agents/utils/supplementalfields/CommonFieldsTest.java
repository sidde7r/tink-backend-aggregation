package se.tink.backend.aggregation.agents.utils.supplementalfields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class CommonFieldsTest {

    @Test
    public void shouldReturnProperSelectionField() {
        // given
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class)))
                .thenAnswer(i -> ((LocalizableKey) i.getArguments()[0]).get());
        when(catalog.getString(any(LocalizableParametrizedKey.class), any()))
                .thenAnswer(i -> ((LocalizableParametrizedKey) i.getArguments()[0]).get());

        // when
        Field result =
                CommonFields.Selection.build(catalog, Arrays.asList("First", "Second", "Third"));

        // then
        assertThat(result.getName()).isEqualTo("selectAuthMethodField");
        assertThat(result.getDescription()).isEqualTo("Choose authentication method");
        assertThat(result.getValue()).isNull();
        assertThat(result.getHint()).isEqualTo("Select from 1 to {0}");
        assertThat(result.getHelpText()).contains("First");
        assertThat(result.getHelpText()).contains("Second");
        assertThat(result.getHelpText()).contains("Third");
        assertThat(result.isNumeric()).isTrue();
        assertThat(result.getMinLength()).isEqualTo(1);
        assertThat(result.getMaxLength()).isEqualTo(1);
        assertThat(result.isImmutable()).isFalse();
        assertThat(result.getPattern()).isNotNull();
        assertThat(result.getPatternError()).isEqualTo("The value you entered is not valid.");
        assertThat(result.getSelectOptions().size()).isEqualTo(3);
        assertThat(result.getSelectOptions().get(0).getText()).isEqualTo("First");
        assertThat(result.getSelectOptions().get(0).getValue()).isEqualTo("1");
        assertThat(result.getSelectOptions().get(1).getText()).isEqualTo("Second");
        assertThat(result.getSelectOptions().get(1).getValue()).isEqualTo("2");
        assertThat(result.getSelectOptions().get(2).getText()).isEqualTo("Third");
        assertThat(result.getSelectOptions().get(2).getValue()).isEqualTo("3");

        verify(catalog, times(2)).getString(any(LocalizableKey.class));
        verify(catalog).getString(any(LocalizableParametrizedKey.class), any());
        verifyNoMoreInteractions(catalog);
    }

    @Test
    public void shouldReturnProperDescriptionOfSelectionField() {
        // given
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class)))
                .thenAnswer(i -> ((LocalizableKey) i.getArguments()[0]).get());

        // when
        Field result =
                CommonFields.Selection.build(
                        catalog, Collections.emptyList(), new LocalizableKey("descriptionToCheck"));

        // then
        assertThat(result.getDescription()).isEqualTo("descriptionToCheck");
    }
}
