package se.tink.backend.connector.cli;

import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.RxJavaUtils;
import se.tink.backend.connector.rpc.seb.ReplayRequest;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class SebReplayTransactionCommand extends ConnectorCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(SebReplayTransactionCommand.class);
    private static final int RUN_IN_SAME_THREAD_CONCURRENCY = 1;
    private static final int MAX_USERS_PER_REQUEST = Integer.getInteger("maxUsersPerReplayRequest", 200);

    public SebReplayTransactionCommand() {
        super("seb-replay",
                "Initiates a replay of transactions with the given external account IDs and dates between ");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {
        log.info("Start SEB replay transaction command.");

        final Date fromDate = getDateSystemProperty("fromDate");
        final Date toDate = getDateSystemProperty("toDate");
        final String url = System.getProperty("url");

        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            log.info("Incorrect date .");
            return;
        }

        if (Strings.isNullOrEmpty(url)) {
            log.info("Missed url, where to post request.");
            return;
        }

        injector.getInstance(UserRepository.class).streamAll()
                .compose(new CommandLineInterfaceUserTraverser(RUN_IN_SAME_THREAD_CONCURRENCY))

                // Converting from User to SEB username (userTokens).
                .map(RxJavaUtils.fromGuavaFunction(User::getUsername))

                .buffer(MAX_USERS_PER_REQUEST)
                .forEach(userTokens -> {

                    Response.StatusType statusInfo = sendRequest(url, userTokens, fromDate, toDate);

                    if (statusInfo.getStatusCode() >= 200 && statusInfo.getStatusCode() < 300) {
                        // Check within 2XX range.

                        log.info("Request was sent successful.");
                    } else {
                        log.warn("Could not send a request: " + statusInfo.getReasonPhrase());
                    }

                });
    }

    private Response.StatusType sendRequest(String url, Iterable<String> userTokens, Date fromDate, Date toDate) {
        ReplayRequest replayRequest = new ReplayRequest(userTokens, fromDate, toDate);
        WebResource webResource = InterContainerJerseyClientFactory.withoutPinning().build().resource(url);
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, replayRequest);
        try {
            return response.getStatusInfo();
        } finally {
            response.close();
        }
    }

    private Date getDateSystemProperty(String property) {
        String inputDate = System.getProperty(property);

        if (Strings.isNullOrEmpty(inputDate)) {
            return null;
        }

        return DateUtils.parseDate(inputDate);
    }
}
