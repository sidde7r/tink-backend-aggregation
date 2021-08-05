package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.LansforsakringarBaseApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class BankIdCollectRequest {
    private final boolean isForCompany = false;
    private final String ip = "::1";
    private final String orderRef;
    private final String clientId = LansforsakringarBaseApiClient.CLIENT_ID;
}
