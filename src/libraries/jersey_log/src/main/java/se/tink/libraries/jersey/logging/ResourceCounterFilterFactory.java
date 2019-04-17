package se.tink.libraries.jersey.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Path;
import org.apache.commons.lang.StringUtils;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.metrics.MetricRegistry;

public class ResourceCounterFilterFactory implements ResourceFilterFactory {

    private final MetricRegistry metricRegistry;

    @Inject
    ResourceCounterFilterFactory(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        String resourcePath = am.getResource().getPath().getValue();
        String methodPath = "";
        Path path = am.getMethod().getAnnotation(Path.class);
        if (path != null) {
            methodPath = path.value();
        }
        String templatePath =
                StringUtils.stripEnd(resourcePath, "/")
                        + "/"
                        + StringUtils.stripStart(methodPath, "/");

        Optional<TeamOwnership> teamOwnership = Optional.empty();
        if (am.getResource().getResourceClass().getPackage().getName().startsWith("se.tink.")) {
            teamOwnership =
                    Optional.of(
                            Preconditions.checkNotNull(
                                    am.getMethod().getAnnotation(TeamOwnership.class),
                                    String.format(
                                            "API method '%s' must be decorated with @TeamOwnership annotation.",
                                            am)));
        }

        return ImmutableList.of(
                new ResourceRequestResponseCountFilter(
                        metricRegistry, templatePath, teamOwnership));
    }
}
