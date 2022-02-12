package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaGetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.CreatePaymentRequestValidator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AllArgsConstructor
public class BoursoramaPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private final TinkHttpClient client;
    private final BoursoramaConfiguration configuration;
    private final CreatePaymentRequestValidator createPaymentRequestValidator;

    @Override
    public void fetchToken() {
        // Boursorama PIS doesn't require token
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request)
            throws PaymentValidationException {
        createPaymentRequestValidator.validate(request);
        return client.request(Urls.CREATE_PAYMENT)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(CreatePaymentResponse.class, BoursoramaPaymentDtoConverter.convert(request));
    }

    @Override
    public String findPaymentId(String authorizationUrl) {
        final List<NameValuePair> queryParams;
        try {
            queryParams = new URIBuilder(authorizationUrl).getQueryParams();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse URL: " + authorizationUrl, e);
        }

        return queryParams.stream()
                .filter(param -> param.getName().equalsIgnoreCase("params[resourceId]"))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot find params[resourceId] in URL: "
                                                + authorizationUrl));
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        final BoursoramaGetPaymentResponse response =
                client.request(Urls.GET_PAYMENT.parameter("paymentId", paymentId))
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .get(BoursoramaGetPaymentResponse.class);

        return BoursoramaPaymentDtoConverter.convert(response);
    }
}
