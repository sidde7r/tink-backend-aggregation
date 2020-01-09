package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues.DEFAULT_DEVICE_ID;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Header.CONNECTION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Header.CONNECTION_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Header.ENCODING_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Header.NB_SIGNATURE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Header.USER_AGENT_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.Header.USER_AGENT_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.ACCOUNT_GENERAL_INFO_ID;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.AUTH_COOKIE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.DEVICE_ID_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.SessionKeys.SESSION_COOKIE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.GET_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.GET_AGGREGATED_SUMMARY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.GET_CARD_DETAILS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.GET_INVESTMENTS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.GET_LOANS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.GET_LOAN_DETAILS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.KEEP_ALIVE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.URLs.LOGIN;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.FieldValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ServiceIds;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail.DigestCalc;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.detail.Login0SecretProvider;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.request.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Request;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Response;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.AccountRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.creditcard.GetCardDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.investment.GetInvestmentsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan.GetLoanAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.request.loan.GetLoanDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.BodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.MovementsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.DetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetCreditCardDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetCreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetSummaryRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.pair.Pair;

public class NovoBancoApiClient {

    private static final Logger logger = LoggerFactory.getLogger(NovoBancoApiClient.class);
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;
    private static final DigestCalc digestCalc = new DigestCalc();

    public NovoBancoApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.httpClient = requireNonNull(client);
        this.sessionStorage = requireNonNull(sessionStorage);
    }

    public Login0Response loginStep0(String userName, String pin) {
        String secret = new Login0SecretProvider().getSecret(pin);
        Login0Request request = new Login0Request(userName, secret, DEFAULT_DEVICE_ID);
        return buildRequest(request, LOGIN).post(Login0Response.class);
    }

    public GetAccountsResponse getAccount(String accountId) {
        GetAccountsResponse response =
                buildRequest(prepareGetAccountsRequest(accountId), GET_ACCOUNTS)
                        .post(GetAccountsResponse.class);
        if (!response.isSuccessful()) {
            logger.error(
                    "ObterLista Response ended up with failure code: {}", response.getResultCode());
            throw new IllegalStateException("Fetching requested account failed");
        }
        return response;
    }

    public GetAccountsResponse getAccounts() {
        GetAccountsResponse response =
                buildRequest(prepareGetAccountsRequest(null), GET_ACCOUNTS)
                        .post(GetAccountsResponse.class);
        if (!response.isSuccessful()) {
            logger.error(
                    "ObterLista Response ended up with failure code: {}", response.getResultCode());
            throw new IllegalStateException("Fetching Transactional Accounts failed");
        }
        saveAccountsInSessionStorage(response);
        return response;
    }

    public Collection<LoanAggregatedData> getLoanAccounts() {
        List<LoanAggregatedData> loans = new ArrayList<>();
        getAccountDetails()
                .forEach(
                        accountDetails -> {
                            GetLoanAccountsResponse loanAccountDetails =
                                    getGetLoanAccountsDetails(accountDetails);

                            String opToken = loanAccountDetails.getHeader().getOpToken();
                            loans.addAll(
                                    getLoansAggregatedData(
                                            accountDetails, loanAccountDetails, opToken));
                        });
        return loans;
    }

    public Pair<List<MovementsEntity>, String> getTransactions(String accountId) {
        GetAccountsResponse details = getAccount(accountId);
        String currency =
                Optional.of(details)
                        .map(GetAccountsResponse::getBody)
                        .map(BodyEntity::getCurrency)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No information about currency available"));
        List<MovementsEntity> movements =
                Optional.of(details)
                        .map(GetAccountsResponse::getBody)
                        .map(BodyEntity::getMovements)
                        .orElse(Collections.emptyList());

        return Pair.of(movements, currency);
    }

    public Collection<GetInvestmentsResponse> getInvestments() {
        List<GetInvestmentsResponse> investmentsResponses = new ArrayList<>();
        getAccountDetails()
                .forEach(
                        accountDetails -> {
                            GetInvestmentsRequest request =
                                    prepareGetInvestmentsRequest(accountDetails.getId());
                            GetInvestmentsResponse response =
                                    buildRequest(request, GET_INVESTMENTS)
                                            .post(GetInvestmentsResponse.class);
                            if (!response.isSuccessful()) {
                                logger.warn(
                                        "ObterCarteiraFundos Response ended up with failure code: {}",
                                        response.getResultCode());
                            }
                            investmentsResponses.add(response);
                        });
        return investmentsResponses;
    }

    public CreditCardAggregatedData getCreditCards() {
        List<GetCreditCardDetailsResponse> cardsDetails = new ArrayList<>();
        GetSummaryResponse summaryResponse = getGetSummaryResponse();

        Collection<String> creditCardContractsIds = summaryResponse.getCreditCardsContractsIds();
        creditCardContractsIds.forEach(
                contractId -> {
                    GetCreditCardDetailsResponse cardResponse =
                            buildRequest(prepareGetCardDetailsRequest(contractId), GET_CARD_DETAILS)
                                    .post(GetCreditCardDetailsResponse.class);
                    if (!cardResponse.isSuccessful()) {
                        logger.error(
                                "ConsultaMovimentosCartoes Request ended up with failure code: {}",
                                cardResponse.getResultCode());
                    }
                    cardsDetails.add(cardResponse);
                });
        return new CreditCardAggregatedData(cardsDetails, getAccountDetails());
    }

    public boolean isAlive() {
        GenericResponse response =
                buildRequest(prepareGetAccountsRequest(null), KEEP_ALIVE)
                        .post(GenericResponse.class);
        return !response.isSessionExpired();
    }

    private Collection<LoanAggregatedData> getLoansAggregatedData(
            AccountDetailsEntity accountDetails,
            GetLoanAccountsResponse loanAccountDetails,
            String opToken) {
        List<LoanAggregatedData> loans = new ArrayList<>();
        Collection<DetailsEntity> loansDetails = loanAccountDetails.getLoanDetails();
        loansDetails.forEach(
                loanDetails -> {
                    GetLoanDetailsResponse loanDetailsResponse =
                            getGetLoanDetails(opToken, loanDetails);
                    if (!loanAccountDetails.isSuccessful()) {
                        logger.warn(
                                "ObterDetalheCreditoHabitacao Response ended up with failure code: {}",
                                loanAccountDetails.getResultCode());
                    }
                    loans.add(
                            new LoanAggregatedData(
                                    accountDetails,
                                    loanDetails.getContract(),
                                    loanDetailsResponse));
                });

        return loans;
    }

    private GetLoanDetailsResponse getGetLoanDetails(String opToken, DetailsEntity loanDetails) {
        return buildRequest(
                        prepareGetLoanDetailsRequest(loanDetails.getContract(), opToken),
                        GET_LOAN_DETAILS)
                .post(GetLoanDetailsResponse.class);
    }

    private GetLoanAccountsResponse getGetLoanAccountsDetails(AccountDetailsEntity accountDetails) {
        return buildRequest(prepareGetLoansRequest(accountDetails.getId()), GET_LOANS)
                .post(GetLoanAccountsResponse.class);
    }

    private GetSummaryResponse getGetSummaryResponse() {
        GetSummaryResponse summaryResponse =
                buildRequest(prepareGetSummaryRequest(), GET_AGGREGATED_SUMMARY)
                        .post(GetSummaryResponse.class);
        if (!summaryResponse.isSuccessful()) {
            logger.error(
                    "PosicaoIntegrada Request ended up with failure code: {}",
                    summaryResponse.getResultCode());
            throw new IllegalStateException("Could not get Credit Card details");
        }
        return summaryResponse;
    }

    private void saveAccountsInSessionStorage(GetAccountsResponse response) {
        List<AccountDetailsEntity> accounts =
                Optional.of(response.getAccountDetailsEntities())
                        .map(Collection::stream)
                        .orElse(Stream.empty())
                        .collect(Collectors.toList());
        sessionStorage.put(ACCOUNT_GENERAL_INFO_ID, accounts);
    }

    private HeaderEntity getHeaderEntityWithOpToken(String opToken) {
        return new HeaderEntity.HeaderEntityBuilder()
                .withDeviceId(sessionStorage.get(DEVICE_ID_KEY))
                .withAuthId(sessionStorage.get(AUTH_COOKIE_KEY))
                .withSessionId(sessionStorage.get(SESSION_COOKIE_KEY))
                .withOpToken(opToken)
                .build();
    }

    private HeaderEntity getHeaderEntity(String context, Integer serviceId) {
        return new HeaderEntity.HeaderEntityBuilder()
                .withDeviceId(sessionStorage.get(DEVICE_ID_KEY))
                .withAuthId(sessionStorage.get(AUTH_COOKIE_KEY))
                .withSessionId(sessionStorage.get(SESSION_COOKIE_KEY))
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
                .header(CONNECTION_KEY, CONNECTION_VALUE)
                .header(HttpHeaders.ACCEPT_ENCODING, ENCODING_VALUE)
                .header(USER_AGENT_KEY, USER_AGENT_VALUE)
                .header(NB_SIGNATURE_KEY, digestCalc.calculateRequestDigest(requestPayload))
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
        GetLoanAccountsEntity body =
                new GetLoanAccountsEntity(FieldValues.LOANS_SECTION_TYPE, accountId);
        return new GetLoanAccountsRequest(header, body);
    }

    private GetLoanDetailsRequest prepareGetLoanDetailsRequest(String contractId, String opToken) {
        HeaderEntity header = getHeaderEntityWithOpToken(opToken);
        GetLoanDetailsBodyEntity body = new GetLoanDetailsBodyEntity(contractId);

        return new GetLoanDetailsRequest(header, body);
    }

    private GetInvestmentsRequest prepareGetInvestmentsRequest(String accountId) {
        HeaderEntity header = getHeaderEntity(FieldValues.CTX_ACCOUNTS, ServiceIds.INVESTMENTS_ID);
        GetInvestmentsBodyEntity body = new GetInvestmentsBodyEntity(accountId, "");
        return new GetInvestmentsRequest(header, body);
    }

    private GetSummaryRequest prepareGetSummaryRequest() {
        HeaderEntity header = getHeaderEntity(FieldValues.CTX_ACCOUNTS, ServiceIds.SUMMARY_ID);
        return new GetSummaryRequest(header);
    }

    private GetCreditCardDetailsRequest prepareGetCardDetailsRequest(String accountCardId) {
        HeaderEntity header = getHeaderEntity(null, ServiceIds.CARDS_ID);
        GetCardDetailsBodyEntity body = new GetCardDetailsBodyEntity(true, accountCardId);
        return new GetCreditCardDetailsRequest(header, body);
    }

    private Collection<AccountDetailsEntity> getAccountDetails() {
        if (!sessionStorage.containsKey(ACCOUNT_GENERAL_INFO_ID)) {
            getAccounts();
        }
        return sessionStorage
                .get(ACCOUNT_GENERAL_INFO_ID, new TypeReference<List<AccountDetailsEntity>>() {})
                .orElse(Collections.emptyList());
    }

    public static class LoanAggregatedData {
        private AccountDetailsEntity accountDetails;
        private String loanContractId;
        private GetLoanDetailsResponse loanDetails;

        public LoanAggregatedData(
                AccountDetailsEntity accountDetails,
                String loanContractId,
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

    public static class CreditCardAggregatedData {
        public CreditCardAggregatedData(
                Collection<GetCreditCardDetailsResponse> creditCardDetailsResponses,
                Collection<AccountDetailsEntity> accountsDetails) {
            this.creditCardDetailsResponses = creditCardDetailsResponses;
            this.accountsDetails = accountsDetails;
        }

        private Collection<GetCreditCardDetailsResponse> creditCardDetailsResponses;
        private Collection<AccountDetailsEntity> accountsDetails;

        public Collection<GetCreditCardDetailsResponse> getCreditCardDetailsResponses() {
            return creditCardDetailsResponses;
        }

        public Collection<AccountDetailsEntity> getAccountsDetails() {
            return accountsDetails;
        }
    }
}
