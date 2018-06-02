package se.tink.backend.common.providers.booli;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.backend.common.providers.booli.entities.request.AuthParameters;
import se.tink.backend.common.providers.booli.entities.request.BooliEstimateRequest;
import se.tink.backend.common.providers.booli.entities.response.BooliEstimateResponse;
import se.tink.backend.common.providers.booli.entities.response.BooliModelMapper;
import se.tink.backend.common.repository.mysql.main.BooliEstimateRepository;
import se.tink.backend.common.repository.mysql.main.BooliSoldPropertyRepository;
import se.tink.backend.core.BooliEstimate;
import se.tink.backend.core.BooliSoldProperty;

public class LookupBooli {
    private static final String ESTIMATE_ROOT_URL = "https://price-estimation.booli.se/api/v1/estimate";
    private static final Joiner.MapJoiner QUERY_MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");

    private final Client client;
    private final BooliEstimateRepository estimateRepository;
    private final BooliSoldPropertyRepository booliSoldPropertyRepository;
    private final AuthParameters authParameters;

    @Inject
    public LookupBooli(BooliEstimateRepository estimateRepository, BooliSoldPropertyRepository booliSoldPropertyRepository, AuthParameters authParameters) {
        this.estimateRepository = estimateRepository;
        this.booliSoldPropertyRepository = booliSoldPropertyRepository;
        this.authParameters = authParameters;
        this.client = new BasicJerseyClientFactory().createCookieClient();
    }

    public BooliEstimate estimate(String propertyId, BooliEstimateRequest request) {
        BooliEstimateResponse response = get(request.toQueryParameters());

        List<BooliSoldProperty> soldProperties = response.getSoldProperties();
        BooliEstimate estimate = BooliModelMapper.booliEstimateResponseToBooliEstimate(propertyId, response);

        booliSoldPropertyRepository.save(soldProperties);
        estimateRepository.save(estimate);

        return estimate;
    }

    private BooliEstimateResponse get(String queryParameters) {
        String url = ESTIMATE_ROOT_URL + "?" + Joiner.on("&").join(
                queryParameters,
                QUERY_MAP_JOINER.join(authParameters.getQueryParameters()));

        return client.resource(url)
                .type(MediaType.WILDCARD_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("User-Agent", "Tink (+https://www.tink.se/; noc@tink.se)")
                .get(BooliEstimateResponse.class);
    }
}
