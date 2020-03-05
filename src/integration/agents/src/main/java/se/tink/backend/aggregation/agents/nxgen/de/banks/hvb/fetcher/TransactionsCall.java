package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsCall.Arg;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionsCall extends SimpleExternalApiCall<Arg, TransactionsResponse> {

    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";

    private final ConfigurationProvider configurationProvider;
    private final HVBStorage storage;

    public TransactionsCall(
            TinkHttpClient httpClient,
            ConfigurationProvider configurationProvider,
            HVBStorage storage) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.storage = storage;
    }

    @Override
    protected HttpRequest prepareRequest(Arg arg) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(
                        configurationProvider.getBaseUrl()
                                + "/adapters/UC_MBX_GL_BE_FACADE_NJ/accounts_requestCurrentAccountTransactionList"),
                prepareRequestHeaders(storage.getAccessToken()),
                prepareRequestBody(arg));
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders(String authorizationToken) {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(AUTHORIZATION, authorizationToken);
        headers.putSingle(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private String prepareRequestBody(Arg arg) {
        return Form.builder()
                .encodeSpacesWithPercent()
                .put("params", prepareBodyParams(arg))
                .build()
                .serialize();
    }

    private String prepareBodyParams(Arg arg) {
        Params params =
                Params.builder()
                        .accountNumber(arg.getAccountNumber())
                        .bookingDateFrom(arg.getDateFrom())
                        .bookingDateTo(arg.getDateTo())
                        .branchNumber(arg.getBranchNumber())
                        .directBankingNumber(arg.directBankingNumber)
                        .build();
        return serializeToString(Collections.singleton(params));
    }

    private String serializeToString(Object obj) {
        return Optional.of(obj)
                .map(SerializationUtils::serializeToString)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't serialize object."));
    }

    @Override
    protected ExternalApiCallResult<TransactionsResponse> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(
                httpResponse.getBody(TransactionsResponse.class), httpResponse.getStatus());
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class Arg {
        private String accountNumber;
        private String branchNumber;
        private String directBankingNumber;
        private LocalDate dateFrom;
        private LocalDate dateTo;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Params {

        private String accountNumber;
        private String branchNumber;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_DATE_FORMAT)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate bookingDateFrom;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_DATE_FORMAT)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate bookingDateTo;

        @JsonProperty("reb")
        private String directBankingNumber;

        private String correlationId = "test1";

        @JsonProperty("isSearch")
        private boolean search = false;

        private int callCounter = 0;
    }
}
