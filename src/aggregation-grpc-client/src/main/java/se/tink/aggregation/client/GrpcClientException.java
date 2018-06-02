package se.tink.aggregation.client;

public class GrpcClientException extends RuntimeException {
    GrpcClientException(Exception cause) {
        super(cause);
    }
}
