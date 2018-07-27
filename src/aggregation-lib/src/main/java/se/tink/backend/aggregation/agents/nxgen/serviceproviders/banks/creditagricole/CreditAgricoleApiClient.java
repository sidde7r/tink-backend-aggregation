package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Authorization;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.ConstantHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.QueryParam;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.AppCodeForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.KeepAliveForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.SelectRegionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.SignInResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.StrongAuthenticationForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.StrongAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc.UserAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.ContractsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.rpc.OperationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CreditAgricoleApiClient {

    private final AggregationLogger log = new AggregationLogger(CreditAgricoleApiClient.class);
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public CreditAgricoleApiClient(TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    /* SIGN IN METHODS */

    public SelectRegionResponse selectRegion() {
        return client.request(Url.SELECT_REGION.parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID)))
                .accept(MediaType.APPLICATION_JSON)
                .get(SelectRegionResponse.class);
    }

    public byte[] numberPad() {
        return client.request(Url.NUMBER_PAD)
                .accept(MediaType.WILDCARD)
                .get(byte[].class);
    }

    public SignInResponse signIn(String userAccountNumber, String userAccountCode) {
        return client.request(Url.SIGN_IN
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                    .queryParam(QueryParam.USER_ACCOUNT_CODE, userAccountCode)
                    .queryParam(QueryParam.USER_ACCOUNT_NUMBER, userAccountNumber))
                .accept(MediaType.APPLICATION_JSON)
                .get(SignInResponse.class);
    }

    public UserAgreementResponse userAgreement() {
        return client.request(Url.USER_AGREEMENT
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID)))
                .get(UserAgreementResponse.class);
    }

    public DefaultResponse appCode(String appCode) {
        AppCodeForm appCodeForm = new AppCodeForm()
                .setUserAccountCode(sessionStorage.get(StorageKey.SHUFFLED_USER_ACCOUNT_CODE))
                .setUserAccountNumber(persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER))
                .setAppCode(appCode);

        return client.request(Url.APP_CODE
                    .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                    .parameter(StorageKey.PARTNER_ID, persistentStorage.get(StorageKey.PARTNER_ID)))
                .body(appCodeForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(DefaultResponse.class);
    }

    public StrongAuthenticationResponse strongAuthentication() {
        StrongAuthenticationForm strongAuthenticationForm = new StrongAuthenticationForm()
                .setUserAccountCode(sessionStorage.get(StorageKey.SHUFFLED_USER_ACCOUNT_CODE))
                .setUserAccountNumber(persistentStorage.get(StorageKey.USER_ACCOUNT_NUMBER))
                .setLogin(persistentStorage.get(StorageKey.LOGIN_EMAIL));

        return client.request(Url.STRONG_AUTHENTICATION
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID)))
                .header(Authorization.HEADER, basicAuth())
                .body(strongAuthenticationForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(StrongAuthenticationResponse.class);
    }

    public DefaultResponse keepAlive() {
        KeepAliveForm keepAliveForm = new KeepAliveForm().SetLlToken(sessionStorage.get(StorageKey.LL_TOKEN));
        return client.request(Url.KEEP_ALIVE
                    .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID)))
                .header(Authorization.HEADER, basicAuth())
                .body(keepAliveForm, MediaType.APPLICATION_FORM_URLENCODED)
                .post(DefaultResponse.class);
    }


    /* ACCOUNTS AND TRANSACTIONS */

    public ContractsResponse contracts() {
        return client.request(Url.CONTRACTS
                    .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                    .parameter(StorageKey.PARTNER_ID, persistentStorage.get(StorageKey.PARTNER_ID)))
                .header(Authorization.HEADER, basicAuth())
                .get(ContractsResponse.class);
    }

    public OperationsResponse operations(String accountNumber) {
        return client.request(Url.OPERATIONS
                    .parameter(StorageKey.USER_ID, persistentStorage.get(StorageKey.USER_ID))
                    .parameter(StorageKey.REGION_ID, persistentStorage.get(StorageKey.REGION_ID))
                    .parameter(StorageKey.PARTNER_ID, persistentStorage.get(StorageKey.PARTNER_ID))
                    .parameter(StorageKey.ACCOUNT_NUMBER, accountNumber))
                .header(Authorization.HEADER, basicAuth())
                .get(OperationsResponse.class);
    }

    /* HELPER METHODS */

    private String basicAuth() {
        return Authorization.BASIC_PREFIX + base64(String.format("%s:%s",
                persistentStorage.get(StorageKey.USER_ID),
                persistentStorage.get(StorageKey.APP_CODE)));
    }

    private String base64(String string) {
        return EncodingUtils.encodeAsBase64String(string);
    }
}
