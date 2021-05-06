package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction;

import static io.vavr.API.$;
import static se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.HEADER_VERSION;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import io.vavr.API;
import io.vavr.control.Either;
import java.util.UUID;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.MetroServiceConstants.Services;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.common.error.UnknownError;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc.TransactionRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.rpc.TransactionResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionClient {
    public final AgentPlatformHttpClient httpClient;

    public TransactionClient(AgentPlatformHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Either<AgentBankApiError, TransactionResponse> fetchTransactions(
            TransactionRequest request) {
        RequestEntity<TransactionRequest> requestEntity =
                RequestEntity.post(Services.MOBILE_APP_SERVICE.url().path("transactions").build())
                        .headers(Services.MOBILE_APP_SERVICE.defaultHeaders())
                        .headers(
                                httpHeaders ->
                                        httpHeaders.add(
                                                "X-REQUEST-ID",
                                                String.format(
                                                        "%s-%s-%s",
                                                        UUID.randomUUID().toString().toUpperCase(),
                                                        GlobalConstants.PLATFORM.getValue(),
                                                        HEADER_VERSION)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request);
        return MetroResponseWrapper.of(
                        httpClient.exchange(requestEntity, String.class), TransactionResponse.class)
                .mapFailure(
                        API.Case(
                                $(res -> res.getStatusCode().isError()),
                                res -> new UnknownError(res.getStatusCode(), res.getBody())))
                .wrap();
    }
}
