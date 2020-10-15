package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyRequestHeaders;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyGetAgreementsRequest extends ProxyRequestMessage<Void> {

    public ProxyGetAgreementsRequest(String type) {
        super(
                "/agreements",
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
                "includeInsights=false&currencies=EUR,XXX&agreementTypes="
                        + type
                        + "&includeEntryPoints=true");
    }
}
