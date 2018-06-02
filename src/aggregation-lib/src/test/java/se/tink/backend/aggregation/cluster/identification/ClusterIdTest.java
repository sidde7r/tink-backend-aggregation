package se.tink.backend.aggregation.cluster.identification;

import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ClusterIdTest {
    private static final String NAME = "clusterName";
    private static final String ENVIRONMENT = "clusterEnvironment";

    @Test(expected = WebApplicationException.class)
    public void whenEmptyClusterId_throwWebApplicationException() {
        ClusterId empty = ClusterId.createEmpty();
        Optional.of(empty)
                .filter(ClusterId::isValidId)
                .orElseThrow(WebApplicationException::new);
    }

    @Test(expected = WebApplicationException.class)
    public void whenEnvironmentNull_throwWebApplicationException() {
        ClusterId validClusterId = ClusterId.create(NAME, null);
        Optional.of(validClusterId)
                .filter(ClusterId::isValidId)
                .orElseThrow(WebApplicationException::new);
    }

    @Test(expected = WebApplicationException.class)
    public void whenNameNull_throwWebApplicationException() {
        ClusterId validClusterId = ClusterId.create(null, ENVIRONMENT);
        Optional.of(validClusterId)
                .filter(ClusterId::isValidId)
                .orElseThrow(WebApplicationException::new);
    }

    @Test
    public void whenValidClusterId_doNotThrow() {
        ClusterId validClusterId = ClusterId.create(NAME, ENVIRONMENT);
        Optional.of(validClusterId)
                .filter(ClusterId::isValidId)
                .orElseThrow(WebApplicationException::new);
    }

    @Test
    public void whenValidClusterId_assertThatClusterIdIsValid() {
        ClusterId clusterId = ClusterId.create(NAME, ENVIRONMENT);
        assertThat(clusterId.isValidId()).isTrue();
    }

    @Test
    public void whenValidClusterId_assertThatIdEqualsExpectedId() {
        ClusterId validClusterId = ClusterId.create(NAME, ENVIRONMENT);
        ClusterId clusterId = Optional.of(validClusterId)
                .filter(ClusterId::isValidId)
                .orElseThrow(WebApplicationException::new);

        assertThat(clusterId.getId()).isEqualToIgnoringCase(String.format("%s-%s", NAME, ENVIRONMENT));
    }
}
