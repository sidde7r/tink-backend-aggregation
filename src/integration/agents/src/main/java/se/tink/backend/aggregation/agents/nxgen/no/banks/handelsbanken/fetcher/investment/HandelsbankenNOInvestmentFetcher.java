package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities.AksjerAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities.OwnCsdAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.AksjerOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.AvailableBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.InitInvestmentsLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.InvestmentsOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc.PositionsListEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class HandelsbankenNOInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final Logger log = LoggerFactory.getLogger(HandelsbankenNOInvestmentFetcher.class);
    private final HandelsbankenNOApiClient apiClient;
    private final String username;

    private HashMap<String, Double> availableBalanceByCsdAccountNumber;

    public HandelsbankenNOInvestmentFetcher(HandelsbankenNOApiClient apiClient, String username) {
        this.apiClient = apiClient;
        this.username = username;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        // The investor portal contains both fund and stock accounts. Current assumption is that for
        // users without
        // investment we get 401 in response. Returning en empty list if that's the case.
        InvestmentsOverviewResponse investmentsOverviewResponse;

        try {
            investmentsOverviewResponse = getInvestmentsOverview();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
                return Collections.emptyList();
            }

            throw e;
        }

        // Handelsbanken's own stock portal contains information about available balance for the
        // stock accounts
        // which is not available from the investor portal. Wrapped with try catch since it's only
        // used for setting
        // cashValue, if something goes wrong still want to return parsed investment accounts.
        try {
            populateAvailableBalanceMap();
        } catch (Exception e) {
            log.warn("Could not fetch data from stock portal (aksjer.handelsbanken.no)", e);
        }

        return investmentsOverviewResponse.getOwnCsdAccounts().stream()
                .filter(OwnCsdAccountEntity::isNotClosed)
                .map(this::convertToTinkAccount)
                .collect(Collectors.toList());
    }

    private InvestmentsOverviewResponse getInvestmentsOverview() {
        String so = getSoToken(HandelsbankenNOConstants.InvestmentConstants.INVESTOR_PORTAL);
        String htmlResponse = apiClient.investorCustomerPortalLogin(so);
        apiClient.finalizeInvestorLogin(
                parseSamlResponse(
                        htmlResponse,
                        HandelsbankenNOConstants.InvestmentConstants.INVESTOR_PORTAL));

        return apiClient.fetchInvestmentsOverview(username);
    }

    private void populateAvailableBalanceMap() {
        availableBalanceByCsdAccountNumber = Maps.newHashMap();
        AksjerOverviewResponse aksjerOverviewResponse = getAksjerOverview();

        aksjerOverviewResponse
                .getData()
                .getCustomerData()
                .getAccounts()
                .forEach(this::setBalanceMapValues);
    }

    private AksjerOverviewResponse getAksjerOverview() {
        String so = getSoToken(HandelsbankenNOConstants.InvestmentConstants.STOCK_PORTAL);
        String htmlResponse = apiClient.aksjerCustomerPortalLogin(so);
        apiClient.finalizeAksjerLogin(
                parseSamlResponse(
                        htmlResponse, HandelsbankenNOConstants.InvestmentConstants.STOCK_PORTAL));

        return apiClient.getAksjerOverview();
    }

    private String getSoToken(String portalType) {
        InitInvestmentsLoginResponse initInvestmentLoginResponse = apiClient.initInvestmentLogin();
        String so = initInvestmentLoginResponse.getSo();
        Preconditions.checkState(
                !Strings.isNullOrEmpty(so),
                String.format("Login to %s unsuccessful, did not receive so token.", portalType));

        return so;
    }

    private String parseSamlResponse(String htmlResponse, String portalType) {
        String samlResponse =
                Jsoup.parse(htmlResponse)
                        .getElementsByAttributeValue(
                                HandelsbankenNOConstants.Tags.NAME,
                                HandelsbankenNOConstants.Tags.SAML_RESPONSE)
                        .first()
                        .val();
        Preconditions.checkState(
                !Strings.isNullOrEmpty(samlResponse),
                String.format(
                        "Login to %s unsuccessful, could not parse saml response from html page.",
                        portalType));

        return samlResponse;
    }

    private void setBalanceMapValues(AksjerAccountEntity account) {
        Preconditions.checkNotNull(account.getCustomerId()); // null check before api call
        AvailableBalanceResponse response =
                apiClient.getAksjerAvailableBalance(username, account.getCustomerId());
        availableBalanceByCsdAccountNumber.put(
                account.getVpsAccountNo(), response.getData().getBalance());
    }

    private InvestmentAccount convertToTinkAccount(OwnCsdAccountEntity csdAccount) {
        PositionsListEntity positions = apiClient.getPositions(csdAccount.getCsdAccountNumber());
        List<PositionEntity> filteredPositionList =
                positions.stream()
                        .filter(positionEntity -> positionEntity.getVolume() != 0)
                        .collect(Collectors.toList());

        return csdAccount.toTinkAccount(filteredPositionList, availableBalanceByCsdAccountNumber);
    }
}
