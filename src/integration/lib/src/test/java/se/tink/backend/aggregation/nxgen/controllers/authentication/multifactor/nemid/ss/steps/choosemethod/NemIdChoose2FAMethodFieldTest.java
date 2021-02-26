package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.choosemethod;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod.CODE_APP;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod.CODE_CARD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod.CODE_TOKEN;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class NemIdChoose2FAMethodFieldTest {

    private Catalog catalog;

    @Before
    public void setup() {
        catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("something not empty");
    }

    @Test
    @Parameters(method = "selectOptionsOrderTestParams")
    public void should_keep_correct_select_options_order(
            List<NemId2FAMethod> methodsPassedToFieldBuilder,
            List<String> expectedSelectOptionKeysOrder) {
        // when
        Field field = NemIdChoose2FAMethodField.build(catalog, methodsPassedToFieldBuilder);

        // then
        List<String> selectOptionValues =
                field.getSelectOptions().stream()
                        .map(SelectOption::getValue)
                        .collect(Collectors.toList());
        assertThat(selectOptionValues).isEqualTo(expectedSelectOptionKeysOrder);
    }

    @SuppressWarnings("unused")
    private Object[] selectOptionsOrderTestParams() {
        return Stream.of(
                        SelectOptionsOrderTestParams.builder()
                                .methodsPassedToFieldBuilder(
                                        asList(CODE_APP, CODE_CARD, CODE_TOKEN))
                                .expectedSelectOptionKeysOrder(
                                        asList(
                                                CODE_APP.getSupplementalInfoKey(),
                                                CODE_CARD.getSupplementalInfoKey(),
                                                CODE_TOKEN.getSupplementalInfoKey()))
                                .build(),
                        SelectOptionsOrderTestParams.builder()
                                .methodsPassedToFieldBuilder(
                                        asList(CODE_APP, CODE_TOKEN, CODE_CARD))
                                .expectedSelectOptionKeysOrder(
                                        asList(
                                                CODE_APP.getSupplementalInfoKey(),
                                                CODE_CARD.getSupplementalInfoKey(),
                                                CODE_TOKEN.getSupplementalInfoKey()))
                                .build(),
                        SelectOptionsOrderTestParams.builder()
                                .methodsPassedToFieldBuilder(asList(CODE_TOKEN, CODE_CARD))
                                .expectedSelectOptionKeysOrder(
                                        asList(
                                                CODE_CARD.getSupplementalInfoKey(),
                                                CODE_TOKEN.getSupplementalInfoKey()))
                                .build())
                .map(SelectOptionsOrderTestParams::toMethodParams)
                .toArray(Object[]::new);
    }

    @Data
    @Builder
    private static class SelectOptionsOrderTestParams {
        private final List<NemId2FAMethod> methodsPassedToFieldBuilder;
        private final List<String> expectedSelectOptionKeysOrder;

        private Object[] toMethodParams() {
            return new Object[] {methodsPassedToFieldBuilder, expectedSelectOptionKeysOrder};
        }
    }
}
