package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.TechnicalRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;

public class KeepAliveRequest extends BelfiusRequest {

    public static KeepAliveRequest.Builder create() {
        return BelfiusRequest.builder().setRequests(
                TechnicalRequest.withRequestType("heartbeat"),
                TechnicalRequest.withRequestType("keepAlive"));
    }
}
