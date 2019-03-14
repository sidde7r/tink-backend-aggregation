package se.tink.backend.integration.gprcserver.configuration;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.grpc.BindableService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import se.tink.backend.integration.gprcserver.GrpcServer;
import se.tink.backend.integration.pingservice.PingService;

public class GrpcServerModule extends AbstractModule {
    @Override
    protected void configure() {
        Binder b = binder();
        List<Class<? extends BindableService>> services = Lists.newArrayList(
                PingService.class
        );

        bindSet(b, BindableService.class, services);
        bind(GrpcServer.class).in(Scopes.SINGLETON);
        bind(SocketAddress.class)
                .annotatedWith(Names.named("grpcSocket"))
                .toInstance(new InetSocketAddress(8443));
        // TODO: get port from the config.
        // TODO: move tls here
    }

    private static <T> void bindSet(Binder binder, Class<T> type,
            List<Class<? extends T>> classes) {
        Multibinder<T> resourceMultibinder = Multibinder.newSetBinder(binder, type, Names.named("grpcServices"));
        classes.forEach(c -> resourceMultibinder.addBinding().to(c).in(Scopes.SINGLETON));
    }
}
