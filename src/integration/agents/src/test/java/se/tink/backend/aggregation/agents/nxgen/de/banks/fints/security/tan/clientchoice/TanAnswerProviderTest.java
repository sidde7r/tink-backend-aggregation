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

public class TanAnswerProviderTest {
    private static SupplementalInformationHelper helper = mock(SupplementalInformationHelper.class);

    @SneakyThrows
    @Test
    public void shouldReturnTanAnswerOfUserChoice() {
        // given
        when(helper.askSupplementalInformation(any())).thenReturn(getSimulatedUserAnswer());
        TanAnswerProvider tanAnswerProvider = new TanAnswerProvider(helper);

        // when
        String tanAnswer = tanAnswerProvider.getTanAnswer();

        // then
        assertThat(tanAnswer).isEqualTo("User TAN answer");
    }

    private Map<String, String> getSimulatedUserAnswer() {
        HashMap<String, String> answer = new HashMap<>();
        answer.put("generatedTAN", "User TAN answer");
        return answer;
    }
}
