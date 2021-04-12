package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_ACTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_VALUE;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.assertj.core.util.VisibleForTesting;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class RuralviaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private RuralviaApiClient apiClient;
    private static final Logger log =
            LoggerFactory.getLogger(RuralviaTransactionalAccountFetcher.class);

    private static final DateTimeFormatter PATTERN = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    public RuralviaTransactionalAccountFetcher(RuralviaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        GlobalPositionResponse globalPosition =
                new GlobalPositionResponse(apiClient.getGlobalPositionHtml());

        // get Accounts From Global Position
        return globalPosition.getAccounts().stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, @Nullable String key) {
        GlobalPositionResponse globalPosition =
                new GlobalPositionResponse(apiClient.getGlobalPositionHtml());
        TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();

        AccountEntity accEntity = getAccEntityForSelectedTinkAccount(account, globalPosition);

        if (accEntity == null) {
            log.error("ERROR: account not found when fetch transactions");
            paginatorResponse.setTransactions(Collections.emptyList());
            return paginatorResponse;
        }

        // First request transactions, go to last transactions page
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(3);

        URL url =
                URL.of(Urls.RURALVIA_SECURE_HOST + accEntity.getForm().attr(ATTRIBUTE_TAG_ACTION));
        String bodyForm = createFormFirstRequestAccountTransaction(accEntity, fromDate, toDate);
        String html =
                apiClient.navigateAccountTransactionFirstRequest(
                        requestBuilderWithForm(url, bodyForm));

        if (html.contains("NO EXISTEN DATOS PARA LA CONSULTA REALIZADA")
                || html.contains(".contains(\"Error de la aplicac\")")
                || html.contains("Error ocurrido en la aplicaci")) {
            log.warn("no transactions for this account");
            paginatorResponse.setTransactions(Collections.emptyList());
            return paginatorResponse;
        }

        // Transactions by dates, TODO implement pagination by dates
        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + Jsoup.parse(html)
                                        .getElementsByAttributeValue("name", "FORM_RVIA_0")
                                        .first()
                                        .attr(ATTRIBUTE_TAG_ACTION));

        bodyForm = requestTransactionsBetweenDatesBodyForm(html, accEntity, fromDate, toDate);

        html =
                apiClient.navigateAccountTransactionsBetweenDates(
                        requestBuilderWithForm(url, bodyForm));

        paginatorResponse = fetchAccountTransactions(html, accEntity.getCurrency());

        return paginatorResponse;
    }

    private TransactionKeyPaginatorResponseImpl<String> fetchAccountTransactions(
            String html, String currency) {

        // contains transactions
        Element form = Jsoup.parse(html).getElementsByTag("FORM").first();

        Elements transactionsTable =
                form.select("div:containsOwn(Cumpliendo con la) ~ table tr").get(1).select("td");
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
                                .setDescription(rows.get(2).ownText())
                                .setAmount(parseAmount(rows.get(3).ownText(), currency))
                                .build();
                tinkTransactions.add(trx);
            } catch (ParseException e) {
                log.warn("WARNING: can not parse the transaction date");
            }

            //
        }
        TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<String>();
        paginatorResponse.setTransactions(tinkTransactions);
        return paginatorResponse;
    }

    /** The Period must be equal or less than 3 months */
    private String requestTransactionsBetweenDatesBodyForm(
            String html, AccountEntity account, LocalDate fromDate, LocalDate toDate) {
        String params;
        if (html.contains(".TRANCODE.value")) {
            params = getParamsForMovements1(html, account, fromDate, toDate);
        } else {
            boolean nextPage = false;
            if (html.contains("onClick=\"siguientes()")) {
                nextPage = true;
            }
            params = getParamsForMovements2(html, fromDate, toDate, nextPage);
        }
        return params;
    }

    // TODO to implement
    private String getParamsForMovements1(
            String html, AccountEntity account, LocalDate fromDate, LocalDate toDate) {
        return null;
    }

    private String getParamsForMovements2(
            String html, LocalDate fromDate, LocalDate toDate, boolean nextPage) {

        Document doc = Jsoup.parse(html);

        String currentPage =
                doc.getElementsByAttributeValue("name", "paginaActual")
                        .first()
                        .attr(ATTRIBUTE_TAG_VALUE);

        String firstTime =
                doc.getElementsByAttributeValue("name", "primeraVez")
                        .first()
                        .attr(ATTRIBUTE_TAG_VALUE);

        Element form = doc.getElementsByAttributeValue("name", "FORM_RVIA_0").first();
        Elements inputs = doc.select("FORM[name=FORM_RVIA_0] > INPUT");

        String accountNum = form.select("[name=CUENTA]").first().attr(ATTRIBUTE_TAG_VALUE);

        Form.Builder builder = Form.builder();
        String value;
        for (Element input : inputs) {

            switch (input.attr("name")) {
                case "FECHAMOVDESDE":
                    value = fromDate.format(PATTERN);
                    break;
                case "FechaDesde":
                    value = fromDate.format(PATTERN);
                    break;
                case "FECHAMOVHASTA":
                    value = toDate.format(PATTERN);
                    break;
                case "FechaHasta":
                    value = toDate.format(PATTERN);
                    break;
                case "primeraVez":
                    value = firstTime;
                    break;
                case "paginaActual":
                    value = currentPage;
                    break;
                case "EXTRA_PARAM_CUENTA":
                    value = accountNum;
                    break;
                case "CUENTA":
                    value = accountNum;
                    break;
                case "fechaComparacion":
                    value = LocalDate.now().format(PATTERN);
                    break;
                case "clavePagina":
                    value = nextPage ? "PAS_MOV_CUENTAS_PAGINAR" : "PAS_MOV_CUENTAS";
                    break;
                case "clavePaginaVolver":
                    value = nextPage ? "PAS_MOV_CUENTAS_PAGINAR" : "MENUP_PAS_MOV_CUENTAS";
                    break;
                default:
                    value = encodeValueAttrToURL(input);
                    break;
            }
            if (!input.attr("name").equalsIgnoreCase("tipoBusqueda")
                    && !input.attr("name").equalsIgnoreCase("ordenBusqueda")
                    && !input.attr("name").equalsIgnoreCase("cuentaSel")
                    && !input.attr("name").equalsIgnoreCase("tamanioPagina")
                    && !input.attr("name").equalsIgnoreCase("campoPaginacion")) {

                builder.put(input.attr("name"), value);
            }
        }
        builder.put("tipoMovimiento", "")
                .put("tipoBusqueda", "1")
                .put("ordenBusqueda", "D")
                .put("tamanioPagina", "50")
                .put("campoPaginacion", "lista");

        return builder.build().serialize();
    }

    private AccountEntity getAccEntityForSelectedTinkAccount(
            TransactionalAccount account, GlobalPositionResponse globalPosition) {
        return globalPosition.getAccounts().stream()
                .filter(
                        accountEntity ->
                                accountEntity.getAccountNumber().equals(account.getAccountNumber()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generate a RequestBuilder with the 50 last transactions for the 3 last months, imitating the
     * bank flow
     *
     * @param formToBody a form url encoded
     * @return RequestBuilder
     */
    public RequestBuilder requestBuilderWithForm(URL url, String formToBody) {
        return apiClient.createBodyFormRequest(url, formToBody);
    }

    private String createFormFirstRequestAccountTransaction(
            AccountEntity account, LocalDate fromDate, LocalDate toDate) {

        Elements inputs = account.getForm().siblingElements().select("input");
        Form.Builder builder = Form.builder();
        for (Element input : inputs) {
            String value;
            switch (input.attr("name")) {
                case "clavePagina":
                    value = "PAS_MOV_CUENTAS_POSGLOB";
                    break;
                case "Nmovs":
                case "tamanioPagina":
                    value = "50";
                    break;
                case "descEstado":
                    value = "TODOS";
                    break;
                case "fechaDesde":
                case "FechaDesde":
                    value = fromDate.format(PATTERN);
                    break;
                case "fechaHasta":
                case "FechaHasta":
                    value = toDate.format(PATTERN);
                    break;
                default:
                    value = encodeValueAttrToURL(input);
                    break;
            }
            builder.put(input.attr("name"), value);
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

    @VisibleForTesting
    public static ExactCurrencyAmount parseAmount(String amountString, String currency) {
        final String amountWithoutSpaces = amountString.replaceAll("[\\s\\u00a0]+", "");

        DecimalFormat decimalFormat =
                new DecimalFormat("0.0", new DecimalFormatSymbols(new Locale("es", "ES")));
        decimalFormat.setParseBigDecimal(true); //

        try {
            return ExactCurrencyAmount.of(decimalFormat.parse(amountWithoutSpaces), currency);
        } catch (ParseException e) {
            log.error("Error parsing the balance", e);
        }
        return ExactCurrencyAmount.inEUR(0); // TODO what should return?
    }
}
