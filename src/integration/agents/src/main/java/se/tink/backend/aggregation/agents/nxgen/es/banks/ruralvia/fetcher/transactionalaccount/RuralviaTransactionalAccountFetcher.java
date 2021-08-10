package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.isIn;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.ACCOUNT_SELECTED;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.CURRENT_PAGE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FIRST_TIME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FROM_DATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.RETURN_PAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.SEARCH_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.SORT_ORDER;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.STATE_DESCRIPTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.TO_DATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_ACTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_NAME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.INPUT_TAG;

import com.google.common.annotations.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.form.Form.Builder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
public class RuralviaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private static final int MAX_REQUESTS = 20;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final RuralviaApiClient apiClient;
    private List<AccountEntity> temporalStorageAccountEntities;
    private boolean hasMoreTransactions;

    public RuralviaTransactionalAccountFetcher(RuralviaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GlobalPositionResponse globalPosition =
                new GlobalPositionResponse(apiClient.getGlobalPositionHtml());
        temporalStorageAccountEntities = globalPosition.getAccounts();

        return temporalStorageAccountEntities.stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();
        AccountEntity accEntity = getAccEntityForSelectedTinkAccount(account);

        if (accEntity == null) {
            log.warn("WARN: account not found when fetch transactions");
            paginatorResponse.setTransactions(Collections.emptyList());
            return paginatorResponse;
        }

        URL url =
                URL.of(Urls.RURALVIA_SECURE_HOST + accEntity.getForm().attr(ATTRIBUTE_TAG_ACTION));
        String bodyForm = createFormFirstRequestAccountTransaction(accEntity, fromDate, toDate);
        String html = apiClient.navigateAccountTransactionFirstRequest(url, bodyForm);

        if (html.contains("NO EXISTEN DATOS PARA LA CONSULTA REALIZADA")
                || html.contains(".contains(\"Error de la aplicac\")")
                || html.contains("Error ocurrido en la aplicaci")) {
            log.warn("WARN: no transactions for this account");
            paginatorResponse.setTransactions(Collections.emptyList());
            return paginatorResponse;
        }

        int count = 0;
        String lastTransactionRefNumber = "";
        List<Transaction> responses = new ArrayList<>();

        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + Jsoup.parse(html)
                                        .getElementsByAttributeValue(
                                                ATTRIBUTE_TAG_NAME, "FORM_RVIA_0")
                                        .first()
                                        .attr(ATTRIBUTE_TAG_ACTION));
        do {
            bodyForm =
                    getParamsWhenNoExistsTrancode(
                            html, fromDate, toDate, hasMoreTransactions, lastTransactionRefNumber);

            html = apiClient.navigateAccountTransactionsBetweenDates(url, bodyForm);
            hasMoreTransactions = hasMoretransaction(html);
            lastTransactionRefNumber = extractLastTransactionRefNumber(html);

            responses.addAll(
                    fetchAccountTransactions(html, account.getExactBalance().getCurrencyCode()));

            count++;
        } while (count < MAX_REQUESTS && hasMoreTransactions);

        paginatorResponse.setTransactions(responses);

        return paginatorResponse;
    }

    private String extractLastTransactionRefNumber(String html) {
        return Jsoup.parse(html)
                .select("input[name=numeroSecuencialApunteLast]")
                .first()
                .attr(ATTRIBUTE_TAG_VALUE);
    }

    private boolean hasMoretransaction(String html) {
        return !Jsoup.parse(html)
                .getElementById("PAGINAR")
                .select("a[onclick*=siguientes()]")
                .isEmpty();
    }

    @VisibleForTesting
    public List<Transaction> fetchAccountTransactions(String html, String currency) {
        Element transactionsForm = Jsoup.parse(html).getElementsByTag("FORM").first();

        Elements transactionsTable =
                transactionsForm.select("div:containsOwn(Cumpliendo con la) ~ table tr");
        transactionsTable.remove(0); // the first one refers to the table headers
        Elements rows;
        Transaction trx;
        List<Transaction> tinkTransactions = new ArrayList<>();
        for (Element transaction : transactionsTable) {
            rows = transaction.select("td");
            try {
                trx =
                        Transaction.builder()
                                .setDate(
                                        new SimpleDateFormat("dd-MM-yyyy")
                                                .parse(rows.get(0).ownText()))
                                .setDescription(getDescriptionFromTable(rows.get(2)))
                                .setAmount(
                                        RuralviaUtils.parseAmount(rows.get(3).ownText(), currency))
                                .build();
                tinkTransactions.add(trx);
            } catch (ParseException e) {
                throw new IllegalStateException(e);
            }
        }

        return tinkTransactions;
    }

    private String getDescriptionFromTable(Element element) {
        if (element.getElementsByTag("a").isEmpty()) {
            return element.ownText();
        } else {
            return element.getElementsByTag("a").get(0).ownText();
        }
    }

    private String getParamsWhenNoExistsTrancode(
            String html,
            Date fromDate,
            Date toDate,
            boolean nextPage,
            String lastTransactionRefNumber) {
        Document doc = Jsoup.parse(html);

        String currentPage =
                doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, CURRENT_PAGE)
                        .first()
                        .attr(ATTRIBUTE_TAG_VALUE);

        String firstTime =
                doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, FIRST_TIME)
                        .first()
                        .attr(ATTRIBUTE_TAG_VALUE);

        Element form = doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, "FORM_RVIA_0").first();
        List<Element> inputs = doc.select("FORM[name=FORM_RVIA_0] > INPUT");

        String accountNum = form.select("[name=CUENTA]").first().attr(ATTRIBUTE_TAG_VALUE);

        ParamsWrapper wrapper =
                ParamsWrapper.builder()
                        .inputs(inputs)
                        .accountNum(accountNum)
                        .currentPage(currentPage)
                        .firstTime(firstTime)
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .nextPage(nextPage)
                        .lastTransactionRefNumber(lastTransactionRefNumber)
                        .build();

        return getInputFormParams(wrapper).build().serialize();
    }

    private Builder getInputFormParams(ParamsWrapper wrapper) {
        Builder builder = Form.builder();
        String value;

        for (Element input : wrapper.inputs) {

            value =
                    Match(input.attr(ATTRIBUTE_TAG_NAME))
                            .of(
                                    Case(
                                            $(isIn("FECHAMOVDESDE", "FechaDesde")),
                                            dateFormat.format(wrapper.fromDate)),
                                    Case(
                                            $(isIn("FECHAMOVHASTA", "FechaHasta")),
                                            dateFormat.format(wrapper.toDate)),
                                    Case($(FIRST_TIME), wrapper.firstTime),
                                    Case($(CURRENT_PAGE), wrapper.currentPage),
                                    Case(
                                            $(isIn("EXTRA_PARAM_CUENTA", "CUENTA")),
                                            wrapper.accountNum),
                                    Case($("fechaComparacion"), dateFormat.format(new Date())),
                                    Case(
                                            $(PAGE_KEY),
                                            wrapper.nextPage
                                                    ? "PAS_MOV_CUENTAS_PAGINAR"
                                                    : "PAS_MOV_CUENTAS"),
                                    Case(
                                            $(RETURN_PAGE_KEY),
                                            wrapper.nextPage
                                                    ? "PAS_MOV_CUENTAS_PAGINAR"
                                                    : "MENUP_PAS_MOV_CUENTAS"),
                                    Case(
                                            $("numeroSecuencialApunteFirst"),
                                            wrapper.lastTransactionRefNumber.isEmpty()
                                                    ? ""
                                                    : wrapper.lastTransactionRefNumber),
                                    Case(
                                            $("numeroSecuencialApunteLast"),
                                            wrapper.lastTransactionRefNumber.isEmpty()
                                                    ? ""
                                                    : String.valueOf(
                                                            Long.parseLong(
                                                                            wrapper.lastTransactionRefNumber)
                                                                    - 50)),
                                    Case($(), input.attr(ATTRIBUTE_TAG_VALUE)));

            String name = input.attr(ATTRIBUTE_TAG_NAME);

            if (!name.equalsIgnoreCase(SEARCH_TYPE)
                    && !name.equalsIgnoreCase(SORT_ORDER)
                    && !name.equalsIgnoreCase(ACCOUNT_SELECTED)) {
                builder.put(name, value);
            }
        }
        return builder;
    }

    private AccountEntity getAccEntityForSelectedTinkAccount(TransactionalAccount account) {
        return temporalStorageAccountEntities.stream()
                .filter(
                        accountEntity ->
                                accountEntity.getAccountNumber().equals(account.getAccountNumber()))
                .findFirst()
                .orElse(null);
    }

    private String createFormFirstRequestAccountTransaction(
            AccountEntity account, Date fromDate, Date toDate) {
        Elements inputs = account.getForm().siblingElements().select(INPUT_TAG);
        Form.Builder builder = Form.builder();
        for (Element input : inputs) {
            String value;
            switch (input.attr(ATTRIBUTE_TAG_NAME)) {
                case PAGE_KEY:
                    value = "PAS_MOV_CUENTAS_POSGLOB";
                    break;
                case "Nmovs":
                case PAGE_SIZE:
                    value = "50";
                    break;
                case STATE_DESCRIPTION:
                    value = "TODOS";
                    break;
                case FROM_DATE:
                case "FechaDesde":
                    value = dateFormat.format(fromDate);
                    break;
                case TO_DATE:
                case "FechaHasta":
                    value = dateFormat.format(toDate);
                    break;
                default:
                    value = encodeValueAttrToURL(input);
                    break;
            }
            builder.put(input.attr(ATTRIBUTE_TAG_NAME), value);
        }

        return builder.build().serialize();
    }

    private String encodeValueAttrToURL(Element element) {
        try {
            return URLEncoder.encode(
                    element.attr(ATTRIBUTE_TAG_VALUE), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @lombok.Builder
    private static class ParamsWrapper {
        String lastTransactionRefNumber;
        String currentPage;
        String firstTime;
        @Singular private List<Element> inputs;
        String accountNum;
        Date fromDate;
        Date toDate;
        boolean nextPage;
    }
}
