package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc.CreditCardRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.rpc.CreditCardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditMutuelApiClient extends EuroInformationApiClient {
    private static final AggregationLogger AGGREGATION_LOGGER =
            new AggregationLogger(CreditMutuelApiClient.class);
    protected final Logger LOGGER = LoggerFactory.getLogger(CreditMutuelApiClient.class);

    public CreditMutuelApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        super(client, sessionStorage, config);
    }

    public CreditCardResponse requestCreditCardAccounts() {
        String response =
                buildRequestHeaders(CreditMutuelConstants.Url.CREDIT_CARD_ACCOUNTS)
                        .post(String.class, new CreditCardRequest());
        return SerializationUtils.deserializeFromString(response, CreditCardResponse.class);
    }
}
