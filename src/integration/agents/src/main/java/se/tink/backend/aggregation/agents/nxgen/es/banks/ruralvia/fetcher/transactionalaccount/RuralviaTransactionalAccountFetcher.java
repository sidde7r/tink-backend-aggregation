package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FROM_DATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.TO_DATE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_ACTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_NAME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.TAG_INPUT;

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
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
public class RuralviaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private RuralviaApiClient apiClient;
    private List<AccountEntity> temporalStorageAccountEntities;

    public RuralviaTransactionalAccountFetcher(RuralviaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GlobalPositionResponse globalPosition =
                new GlobalPositionResponse(apiClient.getGlobalPositionHtml());
        temporalStorageAccountEntities = globalPosition.getAccounts();

        // get Accounts From Global Position
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

        // First request transactions, go to last transactions page

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

        paginatorResponse = fetchAccountTransactions(html, accEntity.getCurrency());

        return paginatorResponse;
    }

    @VisibleForTesting
    public TransactionKeyPaginatorResponseImpl<String> fetchAccountTransactions(
            String html, String currency) {

        // contains transactions
        Element form = Jsoup.parse(html).getElementsByTag("FORM").first();

        Elements transactionsTable = form.select("div:containsOwn(Cumpliendo con la) ~ table tr");
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
                log.warn("WARNING: can not parse the transaction date");
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
                doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, "paginaActual")
                        .first()
                        .attr(ATTRIBUTE_TAG_VALUE);

        String firstTime =
                doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, "primeraVez")
                        .first()
                        .attr(ATTRIBUTE_TAG_VALUE);

        Element form = doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, "FORM_RVIA_0").first();
        Elements inputs = doc.select("FORM[name=FORM_RVIA_0] > INPUT");

        String accountNum = form.select("[name=CUENTA]").first().attr(ATTRIBUTE_TAG_VALUE);

        Form.Builder builder = Form.builder();
        String value;
        for (Element input : inputs) {

            String name = input.attr(ATTRIBUTE_TAG_NAME);
            switch (name) {
                case "FECHAMOVDESDE":
                case "FechaDesde":
                    value = dateFormat.format(fromDate);
                    break;
                case "FECHAMOVHASTA":
                case "FechaHasta":
                    value = dateFormat.format(toDate);
                    break;
                case "primeraVez":
                    value = firstTime;
                    break;
                case "paginaActual":
                    value = currentPage;
                    break;
                case "EXTRA_PARAM_CUENTA":
                case "CUENTA":
                    value = accountNum;
                    break;
                case "fechaComparacion":
                    value = dateFormat.format(new Date());
                    break;
                case "clavePagina":
                    value = nextPage ? "PAS_MOV_CUENTAS_PAGINAR" : "PAS_MOV_CUENTAS";
                    break;
                case "clavePaginaVolver":
                    value = nextPage ? "PAS_MOV_CUENTAS_PAGINAR" : "MENUP_PAS_MOV_CUENTAS";
                    break;
                default:
                    value = input.attr(ATTRIBUTE_TAG_VALUE);
                    break;
            }
            if (!name.equalsIgnoreCase("tipoBusqueda")
                    && !name.equalsIgnoreCase("ordenBusqueda")
                    && !name.equalsIgnoreCase("cuentaSel")
                    && !name.equalsIgnoreCase(PAGE_SIZE)
                    && !name.equalsIgnoreCase("campoPaginacion")) {

                builder.put(name, value);
            }
        }
        builder.put("tipoMovimiento", "")
                .put("tipoBusqueda", "1")
                .put("ordenBusqueda", "D")
                .put(PAGE_SIZE, "50")
                .put("campoPaginacion", "lista");

        return builder.build().serialize();
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

        Elements inputs = account.getForm().siblingElements().select(TAG_INPUT);
        Form.Builder builder = Form.builder();
        for (Element input : inputs) {
            String value;
            switch (input.attr(ATTRIBUTE_TAG_NAME)) {
                case "clavePagina":
                    value = "PAS_MOV_CUENTAS_POSGLOB";
                    break;
                case "Nmovs":
                case PAGE_SIZE:
                    value = "50";
                    break;
                case "descEstado":
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
            log.error("error while encoding from Element", e);
        }
        return null;
    }
}
