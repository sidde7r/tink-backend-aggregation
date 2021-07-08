package se.tink.backend.fake_aggregation_controller;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.fake_aggregation_controller.state_controller.FakeBankStateController;

public class FakeAggregationController extends Application<Configuration> {

    private static final Logger log = LoggerFactory.getLogger(FakeAggregationController.class);
    private Map<String, List<String>> callbacksForControllerEndpoints = new ConcurrentHashMap<>();
    private final boolean debugMode;

    public static void main(String[] args) throws Exception {
        log.info("Starting FakeAggregationController");
        boolean debugModeInArgs = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--debug_mode")) {
                debugModeInArgs = true;
                break;
            }
        }
        new FakeAggregationController(debugModeInArgs).run(new String[] {"server"});
        log.info("Started FakeAggregationController");
    }

    public FakeAggregationController(final boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public void initialize(Bootstrap<Configuration> b) {
        // noop
    }

    @Override
    public void run(Configuration c, Environment e) {
        // Add a dummy health check to avoid an annoying warning on startup.
        e.healthChecks()
                .register(
                        "cache",
                        new HealthCheck() {
                            @Override
                            protected Result check() {
                                return Result.healthy();
                            }
                        });

        e.jersey().register(new DataController());
        e.jersey().register(new ResetController());
        e.jersey().register(new PingController());
        e.jersey().register(FakeBankStateController.getInstance());
    }

    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public class PingController {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public Response ping() {
            return Response.ok("pong").build();
        }
    }

    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public class ResetController {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response resetCache() {
            callbacksForControllerEndpoints = new ConcurrentHashMap<>();
            return Response.ok().build();
        }
    }

    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public class DataController {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response getData() {
            return Response.ok(callbacksForControllerEndpoints).build();
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public Response postData(Map<String, String> data) {
            for (final Entry<String, String> entry : data.entrySet()) {
                final String key = entry.getKey();
                if (!callbacksForControllerEndpoints.containsKey(key)) {
                    callbacksForControllerEndpoints.put(key, new ArrayList<>());
                }
                callbacksForControllerEndpoints.get(key).add(entry.getValue());
            }

            if (debugMode) {
                synchronized (callbacksForControllerEndpoints) {
                    log.info(data.toString());
                }
            }

            return Response.status(Status.OK).build();
        }
    }
}
