package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;

public class TanAnswerProviderTest {
    private static SupplementalInformationHelper helper = mock(SupplementalInformationHelper.class);
    private static Catalog catalog = Catalog.getCatalog("EN_US");

    @SneakyThrows
    @Test
    public void shouldReturnTanAnswerOfUserChoice() {
        // given
        when(helper.askSupplementalInformation(any())).thenReturn(getSimulatedUserAnswer());
        TanAnswerProvider tanAnswerProvider = new TanAnswerProvider(helper, catalog);

        // when
        String tanAnswer = tanAnswerProvider.getTanAnswer("dummyTanMedium");

        // then
        assertThat(tanAnswer).isEqualTo("User TAN answer");
    }

    private Map<String, String> getSimulatedUserAnswer() {
        HashMap<String, String> answer = new HashMap<>();
        answer.put("tanField", "User TAN answer");
        return answer;
    }
}
