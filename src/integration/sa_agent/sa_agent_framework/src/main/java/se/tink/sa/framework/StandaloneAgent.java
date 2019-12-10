package se.tink.sa.framework;

import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.tink.sa.framework.service.AuthenticationService;

@Slf4j
public class StandaloneAgent {

    @Autowired private AuthenticationService authenticationService;

    @Value("${server.agent.grpc.port}")
    private int grpServerPort;

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        //        Server server =
        //
        // ServerBuilder.forPort(grpServerPort).addService(authenticationService).build();
        //        server.start();
        //        log.info("Server started");
        //        server.awaitTermination();
    }
}
