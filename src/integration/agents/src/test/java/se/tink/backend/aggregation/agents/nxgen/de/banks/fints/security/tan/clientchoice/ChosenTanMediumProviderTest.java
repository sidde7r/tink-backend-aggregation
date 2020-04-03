package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class ChosenTanMediumProviderTest {
    private static SupplementalInformationHelper helper = mock(SupplementalInformationHelper.class);
    private static FinTsDialogContext context = mock(FinTsDialogContext.class);

    @SneakyThrows
    @Test
    public void shouldReturnTanMediumWhichUserSelected() {
        // given
        when(context.getTanMediumList()).thenReturn(tanMediumOptions());
        when(helper.askSupplementalInformation(any())).thenReturn(getSimulatedUserAnswer());
        ChosenTanMediumProvider tanMediumProvider = new ChosenTanMediumProvider(helper);

        // when
        String tanMedium = tanMediumProvider.getTanMedium(context);

        // then
        assertThat(tanMedium).isEqualTo("Tan Medium 2");
    }

    private Map<String, String> getSimulatedUserAnswer() {
        HashMap<String, String> answer = new HashMap<>();
        answer.put("tanMedium", "1");
        return answer;
    }

    private List<String> tanMediumOptions() {
        return Arrays.asList("Tan Medium 1", "Tan Medium 2", "Tan Medium 3");
    }
}
