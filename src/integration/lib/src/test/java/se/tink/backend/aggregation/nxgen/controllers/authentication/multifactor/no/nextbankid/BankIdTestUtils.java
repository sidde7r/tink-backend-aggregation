package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdTestUtils {

    public static void verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
            Throwable t, AgentException agentException) {

        assertThat(t).isInstanceOf(AgentException.class);

        AgentException e = (AgentException) t;
        assertThat(e.getError()).isEqualTo(agentException.getError());
        assertThat(e.getUserMessage().get()).isEqualTo(agentException.getUserMessage().get());
    }

    public static WebElement mockWebElementWithText(String text) {
        WebElement element = mock(WebElement.class);
        when(element.getText()).thenReturn(text);
        return element;
    }
}
