package se.tink.libraries.jersey.guice;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckReturnValue;
import javax.annotation.PostConstruct;
import javax.inject.Named;

/**
 * Binds in Guice context and registers Jersey resources/filters/filter factories. Relies on
 * Governator to do so, so LifecycleInjector is required for this class to work. Because it handles
 * binding as well, there's no need to bind the resources manually.
 *
 * <p>Example of usage in a Guice module:
 *
 * <pre>
 * JerseyResourceRegistrar.build()
 *     .binder(binder())  // the binder() is available in modules
 *     .addResources(UserJerseyResource.class, AccountJerseyResource.class)
 *     .addFilterFactories(AuthorizationFilterFactory.class)
 *     .addRequestFilters(LoggingFilter.class)
 *     .addResponseFilters(LoggingFilters.class)
 *     .bind()  // binds all the resources and JerseyResourceRegistrar itself
 * </pre>
 */
public class JerseyResourceRegistrar {
    private static final String REQUEST_FILTERS_BINDING = "requestFilters";
    private static final String RESOURCES_BINDING = "resources";
    private static final String FILTER_FACTORIES_BINDING = "filterFactories";
    private static final String RESPONSE_FILTERS_BINDING = "responseFilters";
    private final JerseyEnvironment jersey;
    private final Set<Object> resources;
    private final Set<ResourceFilterFactory> filterFactories;
    private final Set<ContainerRequestFilter> requestFilters;
    private final Set<ContainerResponseFilter> responseFilters;

    public static class Builder {
        private Binder binder;
        private List<Class<?>> resourceClasses = new ArrayList<>();
        private List<Class<? extends ResourceFilterFactory>> filterFactoryClasses =
                new ArrayList<>();
        private List<Class<? extends ContainerRequestFilter>> requestFilterClasses =
                new ArrayList<>();
        private List<Class<? extends ContainerResponseFilter>> responseFilterClasses =
                new ArrayList<>();
        private JerseyEnvironment jersey;

        private Builder() {}

        public Builder binder(Binder binder) {
            this.binder = binder;
            return this;
        }

        public Builder jersey(JerseyEnvironment jersey) {
            this.jersey = jersey;
            return this;
        }

        @CheckReturnValue
        public Builder addResources(Class<?>... resourceClasses) {
            this.resourceClasses.addAll(Arrays.asList(resourceClasses));
            return this;
        }

        @CheckReturnValue
        @SafeVarargs
        public final Builder addFilterFactories(
                Class<? extends ResourceFilterFactory>... filterFactoryClasses) {
            this.filterFactoryClasses.addAll(Arrays.asList(filterFactoryClasses));
            return this;
        }

        @CheckReturnValue
        @SafeVarargs
        public final Builder addRequestFilters(
                Class<? extends ContainerRequestFilter>... requestFilterClasses) {
            this.requestFilterClasses.addAll(Arrays.asList(requestFilterClasses));
            return this;
        }

        @CheckReturnValue
        @SafeVarargs
        public final Builder addResponseFilters(
                Class<? extends ContainerResponseFilter>... responseFilterClasses) {
            this.responseFilterClasses.addAll(Arrays.asList(responseFilterClasses));
            return this;
        }

        public void bind() {
            Preconditions.checkNotNull(
                    binder, "binder can't be null. Set it with binder() method.");
            Preconditions.checkNotNull(
                    jersey, "jersey can't be null. Set it with jersey() method.");
            binder.bind(JerseyEnvironment.class).toInstance(jersey);
            binder.bind(JerseyResourceRegistrar.class).in(Scopes.SINGLETON);
            bindResources(binder, resourceClasses);
            bindFilterFactories(binder, filterFactoryClasses);
            bindRequestFilters(binder, requestFilterClasses);
            bindResponseFilters(binder, responseFilterClasses);
        }
    }

    public static Builder build() {
        return new Builder();
    }

    private static void bindResources(Binder binder, List<Class<?>> resourceClasses) {
        bindSet(binder, RESOURCES_BINDING, Object.class, resourceClasses);
    }

    private static void bindFilterFactories(
            Binder binder, List<Class<? extends ResourceFilterFactory>> filterFactoryClasses) {
        bindSet(
                binder,
                FILTER_FACTORIES_BINDING,
                ResourceFilterFactory.class,
                filterFactoryClasses);
    }

    private static void bindRequestFilters(
            Binder binder, List<Class<? extends ContainerRequestFilter>> requestFilterClasses) {
        bindSet(
                binder,
                REQUEST_FILTERS_BINDING,
                ContainerRequestFilter.class,
                requestFilterClasses);
    }

    private static void bindResponseFilters(
            Binder binder, List<Class<? extends ContainerResponseFilter>> responseFilterClasses) {
        bindSet(
                binder,
                RESPONSE_FILTERS_BINDING,
                ContainerResponseFilter.class,
                responseFilterClasses);
    }

    private static <T> void bindSet(
            Binder binder, String bindingName, Class<T> type, List<Class<? extends T>> classes) {
        Multibinder<T> resourceMultibinder =
                Multibinder.newSetBinder(binder, type, Names.named(bindingName));
        classes.forEach(c -> resourceMultibinder.addBinding().to(c).in(Scopes.SINGLETON));
    }

    @Inject
    private JerseyResourceRegistrar(
            JerseyEnvironment jersey,
            @Named(RESOURCES_BINDING) Set<Object> resources,
            @Named(FILTER_FACTORIES_BINDING) Set<ResourceFilterFactory> filterFactories,
            @Named(REQUEST_FILTERS_BINDING) Set<ContainerRequestFilter> requestFilters,
            @Named(RESPONSE_FILTERS_BINDING) Set<ContainerResponseFilter> responseFilters) {
        this.jersey = jersey;
        this.resources = resources;
        this.filterFactories = filterFactories;
        this.requestFilters = requestFilters;
        this.responseFilters = responseFilters;
    }

    @PostConstruct
    @SuppressWarnings({"unchecked", "unused"})
    private void register() {
        resources.forEach(jersey::register);
        filterFactories.forEach(jersey.getResourceConfig().getResourceFilterFactories()::add);
        requestFilters.forEach(jersey.getResourceConfig().getContainerRequestFilters()::add);
        responseFilters.forEach(jersey.getResourceConfig().getContainerResponseFilters()::add);
    }
}
