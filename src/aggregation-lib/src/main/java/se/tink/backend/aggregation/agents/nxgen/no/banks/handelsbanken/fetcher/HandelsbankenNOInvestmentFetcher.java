package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import org.assertj.core.util.Strings;
import org.jsoup.Jsoup;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc.InitInvestorLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class HandelsbankenNOInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final HandelsbankenNOApiClient apiClient;

    public HandelsbankenNOInvestmentFetcher(HandelsbankenNOApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        InitInvestorLoginResponse initInvestorLoginResponse = apiClient.initInvestorLogin();
        String so = initInvestorLoginResponse.getSo();
        Preconditions.checkState(!Strings.isNullOrEmpty(so),
                "Login to investor unsuccessful, did not recieve so token.");

        String htmlResponse = apiClient.customerPortalLogin(so);
        String samlResponse = Jsoup.parse(htmlResponse).getElementsByAttributeValue(
                HandelsbankenNOConstants.Tags.NAME, HandelsbankenNOConstants.Tags.SAML_RESPONSE ).first().val();
        Preconditions.checkState(!Strings.isNullOrEmpty(samlResponse),
                "Login to investor unsuccessful, could not pars saml response from html page.");

        apiClient.finalizeInvestorLogin(samlResponse);


        return Collections.emptyList();
    }
}
