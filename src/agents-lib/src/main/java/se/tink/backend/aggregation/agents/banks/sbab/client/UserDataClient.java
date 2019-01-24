package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.util.Maps;
import com.google.api.client.util.Strings;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.banks.sbab.exception.UnacceptedTermsAndConditionsException;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.AccountEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.AccountsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.LoanEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.LoanResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.SignFormRequestBody;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.TransactionEntity;
import se.tink.backend.aggregation.utils.json.JsonUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.system.rpc.Loan;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UserDataClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(UserDataClient.class);

    private static final String ACCOUNT_INFO_URL = SECURE_BASE_URL + "/konto/kontoinformation?kontonummer=%s";
    private static final String ACCOUNTS_OVERVIEW_URL = SECURE_BASE_URL + "/meny/sparkontomeny";
    private static final String LOAN_URL = SECURE_BASE_URL + "/secure-rest/rest/lan";
    private static final String AMORTIZATION_DOCUMENTATION_URL = SECURE_BASE_URL + "/secure-rest/rest/amorteringskrav/ejomfattad/%s";
    private static final String LOAN_DETAILS_URL = SECURE_BASE_URL + "/privat/lan/mina_lan/detaljer.html?lanenummer=%s";
    private static final String APPLICATION_PDF = "application/pdf";

    public UserDataClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    public List<AccountEntity> getAccounts() throws Exception {
        String accountOverview = portletResponseToValidJson(createHtmlRequest(ACCOUNTS_OVERVIEW_URL).get(String.class));

        // Parse the accounts overview if we got valid json
        if (JsonUtils.isValidJson(accountOverview)) {
            return SerializationUtils.deserializeFromString(accountOverview, AccountsResponse.class).getAccounts();
        }

        Document document = Jsoup.parse(accountOverview);

        Element form = document.select("form[id=InitRegisterUser]").first();

        if (form == null) {
            throw new IllegalStateException("Could not retrieve account information.");
        }

        String url = SECURE_BASE_URL + form.attr("action");

        throw new UnacceptedTermsAndConditionsException(url, getFormData(form));
    }

    public SignFormRequestBody initiateTermsAndConditionsSigning(String url, MultivaluedMapImpl input)
            throws Exception {
        ClientResponse initiateResponse = createFormEncodedHtmlRequest(url).post(ClientResponse.class, input);

        ClientResponse redirectResponse = createGetRequest(getRedirectUrl(initiateResponse, SECURE_BASE_URL));

        Document document = Jsoup.parse(redirectResponse.getEntity(String.class));

        Element form = document.select("form[id=signFormNexus]").first();

        if (form == null) {
            throw new IllegalStateException("Could not find form to sign.");
        }

        return SignFormRequestBody.from(form);
    }

    private boolean hasMoreTransactions(Document searchResultPage, int pageNumber) {
        Element potentialNextPageLink = searchResultPage.select("li[id=next-link]").first();

        if (potentialNextPageLink != null) {
            String potentialNextPageIndex = potentialNextPageLink.attr("onclick").replaceAll("[^\\d.]", "");
            if (!Strings.isNullOrEmpty(potentialNextPageIndex) && pageNumber >= Integer
                    .parseInt(potentialNextPageIndex)) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public FetchTransactionsResponse fetchTransactions(String accountNumber, int pageNumber,
            FetchTransactionsResponse response) throws Exception {
        Document searchResultPage = getTransactionsPage(accountNumber, pageNumber, response);
        FetchTransactionsResponse newResponse = buildTransactionsResponse(searchResultPage);
        newResponse.setHasMoreResults(hasMoreTransactions(searchResultPage, pageNumber));
        return newResponse;
    }

    public FetchTransactionsResponse initiateTransactionSearch(String accountNumber) throws Exception {
        String accountInfoUrl = String.format(ACCOUNT_INFO_URL, accountNumber);
        Document accountPage = getJsoupDocument(accountInfoUrl);

        FetchTransactionsResponse response = buildTransactionsResponse(accountPage);

        if (hasUpcomingTransactions(accountPage)) {
            response.setUpcomingTransactions(getUpcomingTransactions(accountPage));
        }

        return response;
    }

    private FetchTransactionsResponse buildTransactionsResponse(Document page) throws JsonProcessingException {
        FetchTransactionsResponse response = new FetchTransactionsResponse();
        Element form = page.select("#genomfordaTransaktionerForm").first();

        if (form != null) {
            response.setStrutsTokenName(form.select("input[name=struts.token.name]").val());
            response.setToken(form.select("input[name=token]").val());
            response.setPostUrl(SECURE_BASE_URL + form.attr("action"));
            response.setTransactions(parseTransactionsFrom(page));
        }

        return response;
    }

    private Document getTransactionsPage(String accountNumber, int pageNumber,
            FetchTransactionsResponse response) throws Exception {
        MultivaluedMapImpl transactionSearchBody = createTransactionBody(response, pageNumber, accountNumber);

        ClientResponse redirectResponse = createFormEncodedHtmlRequest(response.getPostUrl())
                .header("Referer", String.format(ACCOUNT_INFO_URL, accountNumber))
                .post(ClientResponse.class, transactionSearchBody);

        return getJsoupDocument(getRedirectUrl(redirectResponse, SECURE_BASE_URL));
    }

    private List<Transaction> parseTransactionsFrom(Elements tableRows) throws JsonProcessingException {
        List<Transaction> transactions = Lists.newArrayList();

        for (int i = 0; i < tableRows.size(); i++) {
            Map<String, String> transactionValues = Maps.newHashMap();

            Element mainRow = tableRows.get(i);
            transactionValues.put("Belopp", mainRow.select("[data-title=Belopp]").text());
            transactionValues.put("Datum", mainRow.select("[data-title=Datum]").text());
            transactionValues.put("Beskrivning", mainRow.select("[data-title=Beskrivning] > a").text());
            transactionValues.put("Typ", mainRow.select("[data-title=Beskrivning] > span").text());

            Element infoRow = tableRows.get(++i);
            Elements headers = infoRow.select("span[class=info-text]");

            Element destinationAccount = headers.select(":contains(Mottagare)").first();

            if (destinationAccount != null) {
                transactionValues.put("Mottagare", destinationAccount.nextSibling().toString());
            }

            Element message = headers.select(":contains(Meddelande)").first();

            if (message != null) {
                transactionValues.put("Meddelande", message.nextSibling().toString());
            }

            i++; // This is a row with a print option which we skip.

            TransactionEntity transactionEntity = SerializationUtils
                    .deserializeFromString(MAPPER.writeValueAsString(transactionValues), TransactionEntity.class);
            Optional<Transaction> transaction = transactionEntity.toTinkTransaction();
            if (transaction.isPresent()) {
                transactions.add(transaction.get());
            } else {
                log.error("Could not convert transaction entity to Tink transaction");
            }
        }

        return transactions;
    }

    private boolean hasUpcomingTransactions(Document accountInfoPage) {
        return accountInfoPage.select("#kommandeContainer > table > tbody > tr").first() != null;
    }

    private List<Transaction> getUpcomingTransactions(Document page) throws JsonProcessingException {
        Elements tableRows = page.select("#kommandeContainer > table > tbody > tr");
        List<Transaction> upcomingTransactions = Lists.newArrayList();

        if (tableRows.first() != null) {
            upcomingTransactions = parseTransactionsFrom(tableRows);
        }

        return upcomingTransactions;
    }

    private List<Transaction> parseTransactionsFrom(Document searchResultPage)
            throws JsonProcessingException {
        Elements tableRows = searchResultPage
                .select("table[class=action-rows accountdetails my-accounts] > tbody > tr");

        if (tableRows != null) {
            return parseTransactionsFrom(tableRows);
        }

        return Lists.newArrayList();
    }

    private MultivaluedMapImpl createTransactionBody(FetchTransactionsResponse response, int currentPage,
            String accountNumber) {
        MultivaluedMapImpl transactionSearchBody = new MultivaluedMapImpl();
        transactionSearchBody.add("aktuellSidaHistorik", currentPage);
        transactionSearchBody.add("kontonummer", accountNumber);
        transactionSearchBody.add("sokbegrepp.valdDatumPeriod", "period5_18months");
        transactionSearchBody.add("traffarPerSida", "100");
        transactionSearchBody.add("sokbegrepp.formattedBeloppMin", "");
        transactionSearchBody.add("sokbegrepp.formattedBeloppMax", "");
        transactionSearchBody.add("sokbegrepp.multisok", "");
        transactionSearchBody.add("struts.token.name", response.getStrutsTokenName());
        transactionSearchBody.add("token", response.getToken());
        return transactionSearchBody;
    }

    public Map<Account, Loan> getLoans() throws Exception {
        Optional<LoanResponse> loanResponse = getLoanResponse();

        if (!loanResponse.isPresent()) {
            return Collections.emptyMap();
        }

        Map<Account, Loan> accountLoanMap = Maps.newHashMap();

        for (LoanEntity loan : loanResponse.get()) {
            Optional<Account> tinkAccount = loan.toTinkAccount();
            Optional<Loan> tinkLoan = loan.toTinkLoan();

            if (tinkLoan.isPresent() && tinkAccount.isPresent()) {
                accountLoanMap.put(tinkAccount.get(), tinkLoan.get());
            } else {
                log.error("Could not convert mortgage entity to Tink account/loan");
            }
        }

        return accountLoanMap;
    }

    public DocumentContainer getAmortizationDocumentation(String loanId) throws Exception {
        String url = String.format(AMORTIZATION_DOCUMENTATION_URL, loanId);

        ClientResponse response = createRequestWithOptionalTypeRefererAndBearerToken(
                url, String.format(LOAN_DETAILS_URL, loanId), MediaType.APPLICATION_OCTET_STREAM)
                .get(ClientResponse.class);

         return new DocumentContainer(APPLICATION_PDF, response.getEntityInputStream());
    }

    private Optional<LoanResponse> getLoanResponse() throws Exception {
        LoanResponse loanResponse = createJsonRequestWithBearer(LOAN_URL).get(LoanResponse.class);
        return Optional.ofNullable(loanResponse);
    }

    private static MultivaluedMapImpl getFormData(Element form) {
        MultivaluedMapImpl data = new MultivaluedMapImpl();

        for (Element input : form.getElementsByTag("input")) {
            data.putSingle(input.attr("name"), input.attr("value"));
        }

        return data;
    }
}
