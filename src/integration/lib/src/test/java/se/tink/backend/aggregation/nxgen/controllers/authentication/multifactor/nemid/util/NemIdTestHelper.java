package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.mockito.exceptions.base.MockitoException;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethodScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics.NemIdMetrics;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.histograms.Histogram;

@Ignore
public class NemIdTestHelper {

    public static void verifyNTimes(Runnable runnable, int times) {
        for (int i = 0; i < times; i++) {
            try {
                runnable.run();
            } catch (Throwable e) {
                String message =
                        String.format(
                                "Verification failed. Verification number: %d / %d", i + 1, times);
                throw new MockitoException(message, e);
            }
        }
    }

    public static void verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
            Throwable t, AgentException agentException) {

        assertThat(t).isInstanceOf(AgentException.class);

        AgentException e = (AgentException) t;
        assertThat(e.getError()).isEqualTo(agentException.getError());
        assertThat(e.getUserMessage().get()).isEqualTo(agentException.getUserMessage().get());
    }

    public static WebElement webElementMockWithText(String text) {
        WebElement webElement = mock(WebElement.class);
        when(webElement.getText()).thenReturn(text);
        return webElement;
    }

    public static WebElement webElementMock() {
        return mock(WebElement.class);
    }

    public static WebElement webElementMock(boolean isDisplayed) {
        WebElement element = mock(WebElement.class);
        when(element.isDisplayed()).thenReturn(isDisplayed);
        return element;
    }

    public static NemIdMetrics nemIdMetricsMock() {
        MetricRegistry metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.histogram(any())).thenReturn(mock(Histogram.class));
        when(metricRegistry.histogram(any(), any())).thenReturn(mock(Histogram.class));
        return new NemIdMetrics(metricRegistry);
    }

    public static Object[] asArray(Object... args) {
        return args;
    }

    public static <T> List<T> asList(T... items) {
        return Stream.of(items).collect(Collectors.toList());
    }

    public static <T> List<T> joinLists(List<T>... lists) {
        return Stream.of(lists)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static List<Tuple2<NemId2FAMethodScreen, NemId2FAMethodScreen>>
            allNemId2FAPairwiseDifferentScreens() {
        List<Tuple2<NemId2FAMethodScreen, NemId2FAMethodScreen>> tuplesList = new ArrayList<>();
        for (NemId2FAMethodScreen firstScreen : NemId2FAMethodScreen.values()) {
            for (NemId2FAMethodScreen secondScreen : NemId2FAMethodScreen.values()) {
                if (firstScreen != secondScreen) {
                    tuplesList.add(new Tuple2<>(firstScreen, secondScreen));
                }
            }
        }
        return tuplesList;
    }
}
