package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.sun.jersey.api.client.Client;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.common.config.SbabIntegrationConfiguration;
import se.tink.backend.aggregation.rpc.Credentials;

public class MortgageSignClient extends BankIdSignClient {

    private static final String BANKID_SIGN_WEBPAGE_PATH = "/sign/%s";
    private static final String NON_VISIBLE_TBS = "Empty tbs";

    public MortgageSignClient(Client client, Credentials credentials, Aggregator aggregator) {
        super(client, credentials, aggregator);
    }

    @Override
    public void setConfiguration(SbabIntegrationConfiguration configuration) {
        super.setConfiguration(configuration);

        if (configuration.getMortgage() != null) {
            // Override the default sign base URL.
            this.signBaseUrl = configuration.getMortgage().getSignBaseUrl();
        }
    }
    
    public SignFormRequestBody initiateSignProcess(String mortgageSignatureId) throws Exception {
        String signWebPageUrl = getSignUrl(BANKID_SIGN_WEBPAGE_PATH, mortgageSignatureId);
        Document signWebPage = getJsoupDocument(signWebPageUrl);

        // Fetch the needed values from the SBAB sign web page.
        Element signForm = signWebPage.select("form[id=nx_sign]").first();
        return SignFormRequestBody.from(signForm);
    }
}
