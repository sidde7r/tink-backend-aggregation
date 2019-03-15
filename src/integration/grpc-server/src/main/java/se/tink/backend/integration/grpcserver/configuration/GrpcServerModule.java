package se.tink.backend.integration.gprcserver.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import se.tink.backend.integration.gprcserver.interceptors.AccessLogInterceptor;
import se.tink.backend.integration.gprcserver.GrpcServer;
import se.tink.backend.integration.gprcserver.interceptors.MonitoringInterceptor;
import se.tink.backend.integration.pingservice.PingService;

public class GrpcServerModule extends AbstractModule {
    @Override
    protected void configure() {
        Binder binder = binder();
        bindServices(binder);
        bindInterceptors(binder);
        bind(GrpcServer.class).in(Scopes.SINGLETON);
        bind(SocketAddress.class)
                .annotatedWith(Names.named("grpcSocket"))
                .toInstance(new InetSocketAddress(8443));
        // TODO: get port from the config.
        // TODO: move tls here
    }

    private void bindServices(Binder binder) {
        List<Class<? extends BindableService>> services = Lists.newArrayList(
                PingService.class
        );

        bindSet(binder, "grpcServices", BindableService.class, services);
    }

    // Order matters - Make sure to have the most important first, probably AccessLogInterceptor.
    private static void bindInterceptors(Binder binder) {
        ImmutableList<Class<? extends ServerInterceptor>> interceptors =
                ImmutableList.of(
                        // Monitoring interceptor is first called since we want to measure the time
                        // of the whole request.
                        MonitoringInterceptor.class,
                        // Access logging interceptor is called before exception interceptor since
                        // we always want to log status
                        // of the response.
                        AccessLogInterceptor.class);

        // The interceptors should be added in the reverse order
        bindSet(binder, "grpcInterceptors", ServerInterceptor.class, interceptors.reverse());
    }

    private static <T> void bindSet(Binder binder, String bindingName, Class<T> type,
            List<Class<? extends T>> classes) {
        Multibinder<T> resourceMultibinder = Multibinder.newSetBinder(binder, type, Names.named(bindingName));
        classes.forEach(c -> resourceMultibinder.addBinding().to(c).in(Scopes.SINGLETON));
    }
}
