package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class BoursoramaPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private final TinkHttpClient client;
    private final BoursoramaConfiguration configuration;

    @Override
    public void fetchToken() {
        // Boursorama PIS doesn't require token
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        return client.request(createUrl(Urls.CREATE_PAYMENT))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(CreatePaymentResponse.class, SerializationUtils.serializeToString(request));
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        return client.request(createUrl(Urls.GET_PAYMENT).parameter("paymentId", paymentId))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .get(GetPaymentResponse.class);
    }

    private URL createUrl(String path) {
        return new URL(configuration.getBaseUrl() + path);
    }
}
