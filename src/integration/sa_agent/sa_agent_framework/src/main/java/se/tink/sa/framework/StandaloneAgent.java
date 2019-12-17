package se.tink.sa.framework;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.tink.sa.framework.service.AuthenticationService;
import se.tink.sa.framework.service.FetchAccountsService;
import se.tink.sa.framework.service.FetchTransactionsService;

@Slf4j
public class StandaloneAgent {

    @Autowired private AuthenticationService authenticationService;

    @Autowired private FetchAccountsService fetchAccountsService;

    @Autowired private FetchTransactionsService fetchTransactionsService;

    @Value(value = "${server.agent.testMode:#{null}}")
    private Boolean testMode;

    @Value("${server.agent.grpc.port}")
    private int grpServerPort;

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        if (BooleanUtils.isNotTrue(testMode)) {
            Server server =
                    ServerBuilder.forPort(grpServerPort)
                            .addService(authenticationService)
                            .addService(fetchAccountsService)
                            .addService(fetchTransactionsService)
                            .build();
            server.start();
            log.info("Server started");
            server.awaitTermination();
        } else {
            log.info("Application is running in test mode. GRPC server is NOT started.");
        }
    }
}
