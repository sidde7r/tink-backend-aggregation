package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyRequestHeaders;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyGetTransactionsRequest extends ProxyRequestMessage<Void> {

    public ProxyGetTransactionsRequest(String path, String query) {
        super(
                path,
                "GET",
                ProxyRequestHeaders.builder()
                        .appVersion(Headers.APP_VERSION_VALUE)
                        .appIdentifier(Headers.APP_IDENTIFIER_VALUE)
                        .deviceModel(Headers.DEVICE_MODEL_VALUE)
                        .osVersion(Headers.OS_VERSION_VALUE)
                        .devicePlatform(Headers.DEVICE_PLATFORM_VALUE)
                        .build(),
                null,
                null,
                query);
    }
}
