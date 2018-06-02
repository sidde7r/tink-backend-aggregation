package se.tink.backend.aggregation.grpc;

import se.tink.aggregation.grpc.RefreshServiceGrpc;

public class RefreshGrpcTransport extends RefreshServiceGrpc.RefreshServiceImplBase {
    // When implementing the service splitting it into multiple specialized
    // endpoints should be considered. It's not a very good idea to couple
    // accounts, transacations, e-invoices transfers in one place.
}
