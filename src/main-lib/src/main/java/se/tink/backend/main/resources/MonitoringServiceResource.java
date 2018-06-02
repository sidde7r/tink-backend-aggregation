package se.tink.backend.main.resources;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.api.MonitoringService;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.health.AbnAmroServiceHealthCheck;
import se.tink.backend.common.health.CheckableHealthCheck;
import se.tink.backend.common.health.CoordinationHealthCheck;
import se.tink.backend.common.health.HealthCheckManager;
import se.tink.backend.common.health.SystemServiceHealthCheck;
import se.tink.backend.common.health.UserServiceHealthCheck;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.http.utils.HttpResponseHelper;

@Path("/api/v1/monitoring")
public class MonitoringServiceResource implements MonitoringService {
    @Context
    private HttpServletRequest request;

    private final ServiceContext serviceContext;

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final CuratorFramework coordinationClient;
    private final ServiceFactory serviceFactory;
    private final SystemServiceFactory systemServiceFactory;

    private final TransactionDao transactionDao;
    private final UserRepository userRepository;

    private final HealthCheckManager healthCheckManager;
    
    public MonitoringServiceResource(final ServiceContext serviceContext) {
        this.serviceContext = serviceContext;

        this.isUseAggregationController = serviceContext.isUseAggregationController();
        this.aggregationControllerCommonClient = serviceContext.getAggregationControllerCommonClient();

        this.aggregationServiceFactory = serviceContext.getAggregationServiceFactory();
        this.coordinationClient = serviceContext.getCoordinationClient();
        this.serviceFactory = serviceContext.getServiceFactory();
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();

        this.transactionDao = serviceContext.getDao(TransactionDao.class);
        this.userRepository = serviceContext.getRepository(UserRepository.class);

        healthCheckManager = new HealthCheckManager(null, buildAllHealthChecks());
    }

    private ImmutableMap<String, HealthCheck> buildAllHealthChecks() {
        Builder<String, HealthCheck> checksBuilder = ImmutableMap.builder();
        checksBuilder.put("coordination", new CoordinationHealthCheck(coordinationClient));

        // Repositories
        checksBuilder.put("transactions", new CheckableHealthCheck(transactionDao));
        checksBuilder.put("users", new CheckableHealthCheck(userRepository));

        // Services
        checksBuilder.put("system_service", new SystemServiceHealthCheck(systemServiceFactory));
        checksBuilder.put("user_service", new UserServiceHealthCheck(serviceFactory));

        // TODO: Check aggregation service.

        if (serviceContext.getConfiguration().getCluster() == Cluster.ABNAMRO) {
            checksBuilder.put("abnamro_service", new AbnAmroServiceHealthCheck(serviceFactory));
        }

        return checksBuilder.build();
    }

    @Override
    public String healthy() {
        boolean draining = this.serviceContext.getApplicationDrainMode().isEnabled();
        if (draining) {
            HttpResponseHelper.error(Status.SERVICE_UNAVAILABLE);
        }

        if (!healthCheckManager.check()) {
            HttpResponseHelper.error(Status.INTERNAL_SERVER_ERROR);
        }
        
        // Note that Cornwall's load balancers assert that the response body is this exact string.
        return "ok";
    }

    @Override
    public String ping(String service) {
        if (Objects.equal(service, "system")) {
            return systemServiceFactory.getUpdateService().ping();
        } else if (Objects.equal(service, "aggregation")) {
            if (isUseAggregationController) {
                return aggregationControllerCommonClient.ping();
            } else {
                return aggregationServiceFactory.getAggregationService().ping();
            }
        } else if (Objects.equal(service, "connector")) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        boolean draining = this.serviceContext.getApplicationDrainMode().isEnabled();
        if (draining) {
            HttpResponseHelper.error(Status.SERVICE_UNAVAILABLE);
        }

        return "pong";
    }
}
