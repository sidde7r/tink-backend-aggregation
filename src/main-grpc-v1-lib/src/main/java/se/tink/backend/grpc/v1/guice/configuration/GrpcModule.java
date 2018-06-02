package se.tink.backend.grpc.v1.guice.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import java.util.List;
import se.tink.backend.grpc.v1.GrpcServer;
import se.tink.backend.grpc.v1.auth.GrpcAuthenticationProvider;
import se.tink.backend.grpc.v1.interceptors.AccessLoggingInterceptor;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.grpc.v1.interceptors.ExceptionInterceptor;
import se.tink.backend.grpc.v1.interceptors.RequestHeadersInterceptor;
import se.tink.backend.grpc.v1.interceptors.RequestTracerInterceptor;
import se.tink.backend.grpc.v1.interceptors.metrics.MonitoringInterceptor;
import se.tink.backend.grpc.v1.transports.AccountGrpcTransport;
import se.tink.backend.grpc.v1.transports.ActivityGrpcTransport;
import se.tink.backend.grpc.v1.transports.ApplicationGrpcTransport;
import se.tink.backend.grpc.v1.transports.authentication.AuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.CalendarGrpcTransport;
import se.tink.backend.grpc.v1.transports.CategoryGrpcTransport;
import se.tink.backend.grpc.v1.transports.ConsentGrpcTransport;
import se.tink.backend.grpc.v1.transports.CredentialGrpcTransport;
import se.tink.backend.grpc.v1.transports.DeviceGrpcTransport;
import se.tink.backend.grpc.v1.transports.authentication.ChallengeResponseAuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.authentication.EmailAndPasswordAuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.FollowGrpcTransport;
import se.tink.backend.grpc.v1.transports.IdentityGrpcTransport;
import se.tink.backend.grpc.v1.transports.InsightsGrpcTransport;
import se.tink.backend.grpc.v1.transports.LoanGrpcTransport;
import se.tink.backend.grpc.v1.transports.authentication.MobileBankIdAuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.authentication.PhoneNumberAndPin6AuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.authentication.PhoneNumberAuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.PingGrpcTransport;
import se.tink.backend.grpc.v1.transports.PropertyGrpcTransport;
import se.tink.backend.grpc.v1.transports.ProviderGrpcTransport;
import se.tink.backend.grpc.v1.transports.SettingsGrpcTransport;
import se.tink.backend.grpc.v1.transports.StatisticGrpcTransport;
import se.tink.backend.grpc.v1.transports.StreamingGrpcTransport;
import se.tink.backend.grpc.v1.transports.TrackingGrpcTransport;
import se.tink.backend.grpc.v1.transports.TransactionGrpcTransport;
import se.tink.backend.grpc.v1.transports.TransferGrpcTransport;
import se.tink.backend.grpc.v1.transports.UserGrpcTransport;
import se.tink.backend.grpc.v1.transports.abnamro.AbnAmroAuthenticationGrpcTransport;
import se.tink.backend.grpc.v1.transports.abnamro.AbnAmroMigrationGrpcTransport;
import se.tink.libraries.cluster.Cluster;

public class GrpcModule extends AbstractModule {
    private Cluster cluster;
    private boolean insightsEnabled;

    public GrpcModule(Cluster cluster, boolean insightsEnabled) {
        this.cluster = cluster;
        this.insightsEnabled = insightsEnabled;
    }

    protected void configure() {
        bind(GrpcServer.class).in(Scopes.SINGLETON);
        bind(GrpcAuthenticationProvider.class).in(Scopes.SINGLETON);
        bindServices(binder());
        bindInterceptors(binder());
    }

    private void bindServices(Binder binder) {

        List<Class<? extends BindableService>> services = Lists.newArrayList(
                AccountGrpcTransport.class,
                ActivityGrpcTransport.class,
                ApplicationGrpcTransport.class,
                AuthenticationGrpcTransport.class,
                CategoryGrpcTransport.class,
                ConsentGrpcTransport.class,
                CredentialGrpcTransport.class,
                DeviceGrpcTransport.class,
                FollowGrpcTransport.class,
                LoanGrpcTransport.class,
                ProviderGrpcTransport.class,
                SettingsGrpcTransport.class,
                StatisticGrpcTransport.class,
                StreamingGrpcTransport.class,
                TrackingGrpcTransport.class,
                TransactionGrpcTransport.class,
                TransferGrpcTransport.class,
                UserGrpcTransport.class,
                PingGrpcTransport.class
        );

        // Bind the cluster specific services
        switch (this.cluster) {
        case TINK:
            services.add(CalendarGrpcTransport.class);
            services.add(EmailAndPasswordAuthenticationGrpcTransport.class);
            services.add(IdentityGrpcTransport.class);

            if (insightsEnabled) {
                services.add(InsightsGrpcTransport.class);
            }

            services.add(MobileBankIdAuthenticationGrpcTransport.class);
            services.add(PropertyGrpcTransport.class);
            break;
        case ABNAMRO:
            services.add(ChallengeResponseAuthenticationGrpcTransport.class);
            services.add(PhoneNumberAuthenticationGrpcTransport.class);
            services.add(PhoneNumberAndPin6AuthenticationGrpcTransport.class);
            services.add(AbnAmroAuthenticationGrpcTransport.class);
            services.add(AbnAmroMigrationGrpcTransport.class);
            break;
        default:
            break;
        }

        bindSet(binder, "grpcServices", BindableService.class, services);
    }

    public static void bindInterceptors(Binder binder) {

        ImmutableList<Class<? extends ServerInterceptor>> interceptors = ImmutableList.of(
                // Monitoring interceptor is first called since we want to measure the time of the whole request.
                MonitoringInterceptor.class,
                // Access logging interceptor is called before exception interceptor since we always want to log status
                // of the response.
                AccessLoggingInterceptor.class,
                ExceptionInterceptor.class,
                RequestTracerInterceptor.class,
                RequestHeadersInterceptor.class,
                AuthenticationInterceptor.class
        );

        // The interceptors should be added in the reverse order
        bindSet(binder, "grpcInterceptors", ServerInterceptor.class, interceptors.reverse());
    }

    private static <T> void bindSet(Binder binder, String bindingName, Class<T> type,
            List<Class<? extends T>> classes) {
        Multibinder<T> resourceMultibinder = Multibinder.newSetBinder(binder, type, Names.named(bindingName));
        classes.forEach(c -> resourceMultibinder.addBinding().to(c).in(Scopes.SINGLETON));
    }

}
