package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues.DEFAULT_DEVICE_ID;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.ACCOUNT_GENERAL_INFO_ID;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.*;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail.DigestCalc;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail.Login0SecretProvider;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Request;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Response;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.AccountRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan.GetLoanAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan.GetLoanDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.BodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.MovementsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.*;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.pair.Pair;

public class NovoBancoApiClient {

    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;
    private final static DigestCalc digestCalc = new DigestCalc();

    public NovoBancoApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.httpClient = requireNonNull(client);
        this.sessionStorage = sessionStorage;
        sessionStorage.put(SessionKeys.DEVICE_ID_KEY, DEFAULT_DEVICE_ID);
    }

    public Login0Response loginStep0(String userName, String pin) throws LoginException {
        String secret = new Login0SecretProvider().getSecret(pin);

        Login0Request request = new Login0Request(userName, secret);

        Login0Response response = buildRequest(request, URLs.LOGIN).post(Login0Response.class);
        if (!response.isValidCredentials()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        sessionStorage.put(SessionKeys.AUTH_COOKIE_KEY, response.getBody().getSession().getAuthCookie());
        sessionStorage.put(SessionKeys.SESSION_COOKIE_KEY, response.getBody().getSession().getSessionCookie());
        sessionStorage.put(SessionKeys.DEVICE_ID_KEY, response.getBody().getDevice().getId());

        return response;
    }

    public GetAccountsResponse getAccount(String accountId) {
        GetAccountsResponse response =
                buildRequest(prepareGetAccountsRequest(accountId), URLs.GET_ACCOUNTS)
                        .post(GetAccountsResponse.class);
        if (!response.isSuccessful()) {
            throw new IllegalStateException("Fetching requested account failed");
        }
        return response;
    }

    public GetAccountsResponse getAccounts() {
        GetAccountsResponse response =
                buildRequest(prepareGetAccountsRequest(null), URLs.GET_ACCOUNTS)
                        .post(GetAccountsResponse.class);
        if (!response.isSuccessful()) {
            throw new IllegalStateException("Fetching Transactional Accounts failed");
        }
        saveAccountsInSessionStorage(response);
        return response;
    }

    public List<LoanAggregatedData> getLoanAccounts() {
        List<LoanAggregatedData> loans = new ArrayList<>();
        getAccountDetails().forEach(accountDetails -> {
            GetLoanAccountsResponse loanAccountDetails = buildRequest(
                    prepareGetLoansRequest(accountDetails.getId()), URLs.GET_LOANS)
                    .post(GetLoanAccountsResponse.class);

            String opToken = loanAccountDetails.getHeader().getOpToken();
            loanAccountDetails.getLoanDetails().forEach(loanDetails -> {
                GetLoanDetailsResponse loanDetailsResponse = buildRequest(
                        prepareGetLoanDetailsRequest(loanDetails.getContract(), opToken), URLs.GET_LOAN_DETAILS)
                        .post(GetLoanDetailsResponse.class);
                loans.add(
                        new LoanAggregatedData(accountDetails, loanDetails.getContract(),
                                 loanDetailsResponse));
            });
        });
        return loans;
    }

    public Pair<List<MovementsEntity>, String> getTransactions(String accountId) {
        GetAccountsResponse details = getAccount(accountId);
        String currency = Optional.of(details)
                .map(GetAccountsResponse::getBody)
                .map(BodyEntity::getCurrency)
                .orElseThrow(() -> new IllegalStateException("No information about currency available"));
        List<MovementsEntity> movements = Optional.of(details)
                .map(GetAccountsResponse::getBody)
                .map(BodyEntity::getMovements)
                .orElse(Collections.emptyList());

        return Pair.of(movements, currency);
    }

    public boolean isAlive() {
        GenericResponse response = buildRequest(prepareGetAccountsRequest(null), URLs.KEEP_ALIVE)
                .post(GenericResponse.class);
        return !response.isSessionExpired();
    }

    private void saveAccountsInSessionStorage(GetAccountsResponse response) {
        List<AccountDetailsEntity> accounts = Optional.of(response.getAccountDetailsEntities())
                .map(Collection::stream).orElse(Stream.empty())
                .collect(Collectors.toList());
        sessionStorage.put(ACCOUNT_GENERAL_INFO_ID, accounts);
    }

    private HeaderEntity getHeaderEntityWithOpToken(String opToken) {
        return new HeaderEntity.HeaderEntityBuilder()
                .withDeviceId(sessionStorage.get(SessionKeys.DEVICE_ID_KEY))
                .withAuthId(sessionStorage.get(SessionKeys.AUTH_COOKIE_KEY))
                .withSessionId(sessionStorage.get(SessionKeys.SESSION_COOKIE_KEY))
                .withOpToken(opToken)
                .build();
    }

    private HeaderEntity getHeaderEntity(String context, Integer serviceId) {
        return new HeaderEntity.HeaderEntityBuilder()
                .withDeviceId(sessionStorage.get(SessionKeys.DEVICE_ID_KEY))
                .withAuthId(sessionStorage.get(SessionKeys.AUTH_COOKIE_KEY))
                .withSessionId(sessionStorage.get(SessionKeys.SESSION_COOKIE_KEY))
                .withContext(context)
                .withServiceId(serviceId)
                .build();
    }

    private RequestBuilder buildRequest(Object requestPayload, URL url) {
        return httpClient
                .request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD)
                .acceptLanguage(Locale.UK)
                .header(Header.CONNECTION_KEY, Header.CONNECTION_VALUE)
                .header(HttpHeaders.ACCEPT_ENCODING, Header.ENCODING_VALUE)
                .header(Header.USER_AGENT_KEY, Header.USER_AGENT_VALUE)
                .header(Header.NB_SIGNATURE_KEY, digestCalc.calculateRequestDigest(requestPayload))
                .body(requestPayload);
    }

    private GetAccountsRequest prepareGetAccountsRequest(String accountId) {
        HeaderEntity header = getHeaderEntity(FieldValues.CTX_ACCOUNTS, ServiceIds.MOVEMENTS_ID);
        return Optional.ofNullable(accountId)
                .map(id -> new GetAccountsRequest(header, new AccountRequestEntity(id)))
                .orElse(new GetAccountsRequest(header));
    }

    private GetLoanAccountsRequest prepareGetLoansRequest(String accountId) {
        HeaderEntity header = getHeaderEntity(FieldValues.CTX_ACCOUNTS, ServiceIds.LOANS_ID);
        GetLoanAccountsEntity body = new GetLoanAccountsEntity(FieldValues.LOANS_SECTION_TYPE, accountId);
        return new GetLoanAccountsRequest(header, body);
    }

    private GetLoanDetailsRequest prepareGetLoanDetailsRequest(String contractId, String opToken) {
        HeaderEntity header = getHeaderEntityWithOpToken(opToken);
        GetLoanDetailsBodyEntity body = new GetLoanDetailsBodyEntity(contractId);

        return new GetLoanDetailsRequest(header, body);
    }

    private Collection<AccountDetailsEntity> getAccountDetails() {
        if (!sessionStorage.containsKey(ACCOUNT_GENERAL_INFO_ID)) {
            getAccounts();
        }
        return sessionStorage.get(ACCOUNT_GENERAL_INFO_ID,
                new TypeReference<List<AccountDetailsEntity>>() {})
                .orElse(Collections.emptyList());
    }

    public static class LoanAggregatedData {
        private AccountDetailsEntity accountDetails;
        private String loanContractId;
        private GetLoanDetailsResponse loanDetails;

        LoanAggregatedData(AccountDetailsEntity accountDetails, String loanContractId,
                           GetLoanDetailsResponse loanDetails) {
            this.accountDetails = accountDetails;
            this.loanContractId = loanContractId;
            this.loanDetails = loanDetails;
        }

        public AccountDetailsEntity getAccountDetails() {
            return accountDetails;
        }

        public String getLoanContractId() {
            return loanContractId;
        }

        public GetLoanDetailsResponse getLoanDetails() {
            return loanDetails;
        }
    }
}
