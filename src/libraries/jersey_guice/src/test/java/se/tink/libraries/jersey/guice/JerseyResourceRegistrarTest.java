package se.tink.libraries.jersey.guice;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Stage;
import com.netflix.governator.guice.LifecycleInjector;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JerseyResourceRegistrarTest {

    static class JerseyResource {}

    static class FilterFactory implements ResourceFilterFactory {
        @Override
        public List<ResourceFilter> create(AbstractMethod abstractMethod) {
            return null;
        }
    }

    static class RequestFilter implements ContainerRequestFilter {
        @Override
        public ContainerRequest filter(ContainerRequest containerRequest) {
            return null;
        }
    }

    static class ResponseFilter implements ContainerResponseFilter {
        @Override
        public ContainerResponse filter(
                ContainerRequest containerRequest, ContainerResponse containerResponse) {
            return null;
        }
    }

    @Mock JerseyEnvironment jersey;
    @Mock ResourceConfig resourceConfig;
    List<ResourceFilterFactory> filterFactories = new ArrayList<>();
    List<ContainerRequestFilter> requestFilters = new ArrayList<>();
    List<ContainerResponseFilter> responseFilters = new ArrayList<>();

    @Before
    public void setUp() {
        when(jersey.getResourceConfig()).thenReturn(resourceConfig);
        when(resourceConfig.getResourceFilterFactories()).thenReturn(filterFactories);
        when(resourceConfig.getContainerRequestFilters()).thenReturn(requestFilters);
        when(resourceConfig.getContainerResponseFilters()).thenReturn(responseFilters);
    }

    @Test
    public void register() {
        class Module extends AbstractModule {
            @Override
            protected void configure() {
                JerseyResourceRegistrar.build()
                        .binder(binder())
                        .jersey(jersey)
                        .addResources(JerseyResource.class)
                        .addFilterFactories(FilterFactory.class)
                        .addRequestFilters(RequestFilter.class)
                        .addResponseFilters(ResponseFilter.class)
                        .bind();
            }
        }
        LifecycleInjector.builder()
                .inStage(Stage.PRODUCTION)
                .withModules(new Module())
                .build()
                .createInjector();

        verify(jersey).register(any(JerseyResource.class));
        assertTrue(filterFactories.get(0) instanceof FilterFactory);
        assertTrue(requestFilters.get(0) instanceof RequestFilter);
        assertTrue(responseFilters.get(0) instanceof ResponseFilter);
    }

    @Test
    public void registerResourcesOnly() {
        class Module extends AbstractModule {
            @Override
            protected void configure() {
                JerseyResourceRegistrar.build()
                        .binder(binder())
                        .jersey(jersey)
                        .addResources(JerseyResource.class)
                        .bind();
            }
        }
        LifecycleInjector.builder()
                .inStage(Stage.PRODUCTION)
                .withModules(new Module())
                .build()
                .createInjector();

        verify(jersey).register(any(JerseyResource.class));
        assertTrue(filterFactories.isEmpty());
        assertTrue(requestFilters.isEmpty());
        assertTrue(responseFilters.isEmpty());
    }

    static class SecondJerseyResource {}

    @Test
    public void subsequentCallsShouldAddResources() {
        class Module extends AbstractModule {
            @Override
            protected void configure() {
                JerseyResourceRegistrar.build()
                        .binder(binder())
                        .jersey(jersey)
                        .addResources(JerseyResource.class)
                        .addResources(SecondJerseyResource.class)
                        .bind();
            }
        }
        LifecycleInjector.builder()
                .inStage(Stage.PRODUCTION)
                .withModules(new Module())
                .build()
                .createInjector();
        verify(jersey).register(any(JerseyResource.class));
        verify(jersey).register(any(SecondJerseyResource.class));
    }

    @Test(expected = NullPointerException.class)
    public void requireBinder() {
        JerseyResourceRegistrar.build().jersey(jersey).addResources(JerseyResource.class).bind();
    }

    @Test(expected = NullPointerException.class)
    public void requireJersey() {
        JerseyResourceRegistrar.build()
                .binder(mock(Binder.class))
                .addResources(JerseyResource.class)
                .bind();
    }
}
