package se.tink.backend.aggregation.aggregationcontroller;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class FakeAggregationController extends Application<Configuration> {

    private static final Logger log = LoggerFactory.getLogger(FakeAggregationController.class);
    private Map<String, List<String>> cache = new HashMap<>();

    public static void main(String[] args) throws Exception {
        log.info("Starting FakeAggregationController");
        new FakeAggregationController().run(new String[] {"server"});
        log.info("Started FakeAggregationController");
    }

    @Override
    public void initialize(Bootstrap<Configuration> b) {}

    @Override
    public void run(Configuration c, Environment e) throws Exception {
        e.jersey().register(new DataController());
        e.jersey().register(new ResetController());
        e.jersey().register(new PingController());
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
            cache = new HashMap<>();
            return Response.ok().build();
        }
    }

    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public class DataController {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response getData() {
            return Response.ok(cache).build();
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public Response putData(Map<String, String> data) {
            for (String key : data.keySet()) {
                if (!cache.containsKey(key)) {
                    cache.put(key, new ArrayList<>());
                }
                cache.get(key).add(data.get(key));
            }

            return Response.status(Status.OK).build();
        }
    }
}
