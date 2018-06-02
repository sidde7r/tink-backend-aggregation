package se.tink.backend.grpc.v1.transports;

import org.junit.Test;
import se.tink.backend.main.transports.ConsentServiceJerseyTransport;

public class ConsentGrpcTransportTest {

    @Test
    public void testConversionBetweenClasses() {
        ConsentGrpcTransport.getModelMapper().validate();
    }
}
