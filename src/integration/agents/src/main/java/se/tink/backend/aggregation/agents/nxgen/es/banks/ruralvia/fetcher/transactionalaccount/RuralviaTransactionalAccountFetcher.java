package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.ACCOUNT_SELECTED;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.CURRENT_PAGE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FIRST_TIME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FROM_DATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGINATION_FIELD;
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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final RuralviaApiClient apiClient;
    private List<AccountEntity> temporalStorageAccountEntities;

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

        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + Jsoup.parse(html)
                                        .getElementsByAttributeValue(
                                                ATTRIBUTE_TAG_NAME, "FORM_RVIA_0")
                                        .first()
                                        .attr(ATTRIBUTE_TAG_ACTION));
        bodyForm = requestTransactionsBetweenDatesBodyForm(html, fromDate, toDate);
        html = apiClient.navigateAccountTransactionsBetweenDates(url, bodyForm);

        return fetchAccountTransactions(html, account.getExactBalance().getCurrencyCode());
    }

    @VisibleForTesting
    public TransactionKeyPaginatorResponseImpl<String> fetchAccountTransactions(
            String html, String currency) {
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

        TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();
        paginatorResponse.setTransactions(tinkTransactions);
        return paginatorResponse;
    }

    private String getDescriptionFromTable(Element element) {
        if (element.getElementsByTag("a").isEmpty()) {
            return element.ownText();
        } else {
            return element.getElementsByTag("a").get(0).ownText();
        }
    }

    /** The Period must be equal or less than 3 months */
    private String requestTransactionsBetweenDatesBodyForm(
            String html, Date fromDate, Date toDate) {
        boolean nextPage = false;
        if (html.contains("onClick=\"siguientes()")) {
            nextPage = true;
        }
        return getParamsWhenNoExistsTrancode(html, fromDate, toDate, nextPage);
    }

    private String getParamsWhenNoExistsTrancode(
            String html, Date fromDate, Date toDate, boolean nextPage) {
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
                        .build();

        return getInputFormParams(wrapper).build().serialize();
    }

    private Builder getInputFormParams(ParamsWrapper wrapper) {
        Builder builder = Form.builder();
        String value;
        for (Element input : wrapper.inputs) {

            String name = input.attr(ATTRIBUTE_TAG_NAME);
            switch (name) {
                case "FECHAMOVDESDE":
                case "FechaDesde":
                    value = dateFormat.format(wrapper.fromDate);
                    break;
                case "FECHAMOVHASTA":
                case "FechaHasta":
                    value = dateFormat.format(wrapper.toDate);
                    break;
                case FIRST_TIME:
                    value = wrapper.firstTime;
                    break;
                case CURRENT_PAGE:
                    value = wrapper.currentPage;
                    break;
                case "EXTRA_PARAM_CUENTA":
                case "CUENTA":
                    value = wrapper.accountNum;
                    break;
                case "fechaComparacion":
                    value = dateFormat.format(new Date());
                    break;
                case PAGE_KEY:
                    value = wrapper.nextPage ? "PAS_MOV_CUENTAS_PAGINAR" : "PAS_MOV_CUENTAS";
                    break;
                case RETURN_PAGE_KEY:
                    value = wrapper.nextPage ? "PAS_MOV_CUENTAS_PAGINAR" : "MENUP_PAS_MOV_CUENTAS";
                    break;
                default:
                    value = input.attr(ATTRIBUTE_TAG_VALUE);
                    break;
            }
            if (!name.equalsIgnoreCase(SEARCH_TYPE)
                    && !name.equalsIgnoreCase(SORT_ORDER)
                    && !name.equalsIgnoreCase(ACCOUNT_SELECTED)
                    && !name.equalsIgnoreCase(PAGE_SIZE)
                    && !name.equalsIgnoreCase(PAGINATION_FIELD)) {

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
        String currentPage;
        String firstTime;
        @Singular private List<Element> inputs;
        String accountNum;
        Date fromDate;
        Date toDate;
        boolean nextPage;
    }
}
