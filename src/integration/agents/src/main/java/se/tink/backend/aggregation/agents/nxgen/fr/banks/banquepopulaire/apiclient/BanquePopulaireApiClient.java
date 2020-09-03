package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.APP_VER;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.OS_VER;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.BANK_CONFIG_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.CHECK_UPDATE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.COMMON_HOST_BASE_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.GENERAL_CONFIG_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.INITIATE_SESSION_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.MESSAGES_SERVICE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants.Urls.TRANSACTIONS_PATH_TEMPLATE;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.account.AccountDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.AccessTokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.AppConfigDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.BankConfigResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.BankResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.CheckUpdateStatusResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize.GeneralConfigurationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.identity.UserIdentityDataDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.transaction.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.converter.BanquePopulaireConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transaction.entity.TransactionResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage.BanquePopulaireStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceCookieParserHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceTokenExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.Saml2PostDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.configuration.BpceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
public class BanquePopulaireApiClient extends BpceApiClient {

    private final BanquePopulaireStorage banquePopulaireStorage;
    private final BanquePopulaireConverter banquePopulaireConverter;

    public BanquePopulaireApiClient(
            TinkHttpClient httpClient,
            BpceConfiguration bpceConfiguration,
            RandomValueGenerator randomValueGenerator,
            BanquePopulaireStorage banquePopulaireStorage,
            BpceTokenExtractor bpceTokenExtractor,
            BanquePopulaireConverter banquePopulaireConverter,
            BpceCookieParserHelper bpceCookieParserHelper) {
        super(
                httpClient,
                bpceConfiguration,
                randomValueGenerator,
                banquePopulaireStorage,
                bpceTokenExtractor,
                bpceCookieParserHelper);
        this.banquePopulaireStorage = banquePopulaireStorage;
        this.banquePopulaireConverter = banquePopulaireConverter;
    }

    public void checkUpdateStatus() {
        final String url =
                String.format(
                        "%s%s?apptype=par&appversion=%s&brand=bp&os=ios&osversion=%s",
                        COMMON_HOST_BASE_URL, CHECK_UPDATE_PATH, APP_VER, OS_VER);

        final CheckUpdateStatusResponseDto checkUpdateStatusResponse =
                baseRequest(url).get(CheckUpdateStatusResponseDto.class);

        verifyCheckUpdateStatusResponse(checkUpdateStatusResponse);
    }

    public GeneralConfigurationResponseDto getGeneralConfig() {
        final String url = COMMON_HOST_BASE_URL + GENERAL_CONFIG_PATH + "?brand=bp&apptype=par";

        return baseRequest(url).get(GeneralConfigurationResponseDto.class);
    }

    public BankConfigResponseDto getBankConfig(BankResourceDto bankResourceDto) {
        final String url =
                String.format(
                        "%s%s?apptype=par&appversion=%s&brand=bp&os=ios",
                        getMobileContextRootUrl(bankResourceDto), BANK_CONFIG_PATH, APP_VER);

        return baseRequest(url).get(BankConfigResponseDto.class);
    }

    public void sendMessageServiceRequest(BankResourceDto bankResourceDto) {
        final String url = getMobileContextRootUrl(bankResourceDto) + MESSAGES_SERVICE_PATH;

        final HttpResponse httpResponse = baseRequest(url).get(HttpResponse.class);

        verifyMessagesServiceResponse(httpResponse);
    }

    public String startSession(
            String username, AppConfigDto appConfigDto, BankResourceDto bankResourceDto) {
        final String url = getInitiateSessionUrl(appConfigDto, bankResourceDto);

        final HttpResponse httpResponse =
                baseRequest(url)
                        .header("NameId", username)
                        .header("PULSAR.Login.GwGroup", "1")
                        .get(HttpResponse.class);

        verifyStartSessionResponse(httpResponse);

        return httpResponse.getLocation().toString();
    }

    public String authorizeSession(String authorizeSessionPath) {
        final HttpResponse httpResponse =
                baseRequest(authorizeSessionPath)
                        .header(
                                HttpHeaders.CONTENT_TYPE,
                                BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                        .get(HttpResponse.class);

        verifyAuthenticateSessionResponse(httpResponse);

        return httpResponse.getLocation().toString();
    }

    public AuthTransactionResponseDto getAuthTransactionForResources(String authTransactionPath) {
        final String url = bpceConfiguration.getIcgAuthBaseUrl() + authTransactionPath;

        return baseRequest(url).get(AuthTransactionResponseDto.class);
    }

    public void sendAcsRequest(Saml2PostDto saml2PostDto) {
        final String requestBody = createOauth2ConsumeRequest(saml2PostDto);

        baseRequest(saml2PostDto.getAction())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class, requestBody);
    }

    public UserIdentityDataDto getUserIdentityData(
            String username, AppConfigDto appConfigDto, BankResourceDto bankResourceDto) {
        final String url = getInitiateSessionUrl(appConfigDto, bankResourceDto);

        return baseRequest(url)
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        BanquePopulaireConstants.Headers.CONTENT_TYPE_JSON_UTF8)
                .header("NameId", username)
                .header("PULSAR.Login.GwGroup", "1")
                .get(UserIdentityDataDto.class);
    }

    public AccessTokenResponseDto retrieveAuxAccessToken(AppConfigDto appConfigDto) {
        final String url =
                appConfigDto.getAuthBaseUrl() + appConfigDto.getWebAPI2().getAuthAccessTokenURL();

        return baseAuthRequest(url)
                .addBasicAuth(bpceConfiguration.getAuthHeaderValue())
                .header(HttpHeaders.ACCEPT, MediaType.WILDCARD)
                .post(AccessTokenResponseDto.class);
    }

    public List<AccountDto> fetchAccounts() {
        final AppConfigDto appConfigDto = banquePopulaireStorage.getAppConfig();
        final BankResourceDto bankResourceDto = banquePopulaireStorage.getBankResource();

        final String url =
                getContextRootUrl(appConfigDto, bankResourceDto)
                        + BanquePopulaireConstants.Urls.ACCOUNTS_PATH;

        final HttpResponse httpResponse = baseRequest(url).get(HttpResponse.class);

        return banquePopulaireConverter.convertHttpResponseBodyToList(
                httpResponse, AccountDto.class);
    }

    public TransactionResponseEntity fetchTransactionsForAccount(
            String apiIdentifier, String paginationKey) {
        final AppConfigDto appConfigDto = banquePopulaireStorage.getAppConfig();
        final BankResourceDto bankResourceDto = banquePopulaireStorage.getBankResource();
        final String paginationKeyValue = Optional.ofNullable(paginationKey).orElse("");

        final String url =
                getContextRootUrl(appConfigDto, bankResourceDto)
                        + String.format(TRANSACTIONS_PATH_TEMPLATE, apiIdentifier);

        final HttpResponse httpResponse =
                baseRequest(url)
                        .queryParam("pageKey", paginationKeyValue)
                        .queryParam("statutMouvement", "000")
                        .get(HttpResponse.class);

        final List<TransactionDto> transactions =
                banquePopulaireConverter.convertHttpResponseBodyToList(
                        httpResponse, TransactionDto.class);
        final String nextKey = parseNextKey(httpResponse).orElse(null);

        return banquePopulaireConverter.convertTransactionDtoListToTransactionResponseEntity(
                transactions, nextKey);
    }

    private static Optional<String> parseNextKey(HttpResponse httpResponse) {
        return Optional.ofNullable(httpResponse.getHeaders().getFirst("Link"))
                .filter(link -> link.contains("rel='next'"))
                .map(link -> link.replace("?pageKey=", "").replace("; rel='next'", ""));
    }

    private static void verifyCheckUpdateStatusResponse(CheckUpdateStatusResponseDto response) {
        if (!response.getStatus().equalsIgnoreCase("OK")) {
            log.warn("Check update status is not \"OK\". Actual value: " + response.getStatus());
        }
    }

    private static void verifyMessagesServiceResponse(HttpResponse httpResponse) {
        if (httpResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
            log.warn(
                    "Message status response status is not 204. Actual value: "
                            + httpResponse.getStatus());
        }
    }

    private static void verifyStartSessionResponse(HttpResponse httpResponse) {
        if (httpResponse.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
            log.warn(
                    "Start session response status is not 302. Actual value: "
                            + httpResponse.getStatus());
        }
    }

    private static void verifyAuthenticateSessionResponse(HttpResponse httpResponse) {
        if (httpResponse.getStatus() != HttpStatus.SC_SEE_OTHER) {
            log.warn(
                    "Authenticate session response status is not 302. Actual value: "
                            + httpResponse.getStatus());
        }
    }

    private RequestBuilder baseRequest(String url) {
        return httpClient
                .request(url)
                .header(
                        BanquePopulaireConstants.Headers.IBP_WEBAPI_CALLERID_NAME,
                        BanquePopulaireConstants.Headers.IBP_WEBAPI_CALLERID)
                .header(HttpHeaders.ACCEPT, MediaType.WILDCARD)
                .header(
                        HttpHeaders.ACCEPT_LANGUAGE,
                        BanquePopulaireConstants.Headers.ACCEPT_LANGUAGE)
                .header(HttpHeaders.USER_AGENT, BanquePopulaireConstants.Headers.USER_AGENT)
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        BanquePopulaireConstants.Headers.CACHE_NO_TRANSFORM);
    }

    private static String getMobileContextRootUrl(BankResourceDto bankResourceDto) {
        return bankResourceDto.getAnoBaseUrl() + bankResourceDto.getApplicationAPIContextRoot();
    }

    private static String getContextRootUrl(
            AppConfigDto appConfigDto, BankResourceDto bankResourceDto) {
        return appConfigDto.getAuthBaseUrl() + bankResourceDto.getApplicationAPIContextRoot();
    }

    private static String getInitiateSessionUrl(
            AppConfigDto appConfigDto, BankResourceDto bankResourceDto) {
        return getContextRootUrl(appConfigDto, bankResourceDto) + INITIATE_SESSION_PATH;
    }
}
