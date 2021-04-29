package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.creditcard;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.*;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils.getURLEncodedUTF8String;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils.parseAmountInEuros;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class RuralviaCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final RuralviaApiClient apiClient;
    private GlobalPositionResponse globalPosition;
    private List<CreditCardEntity> temporalStorageCreditCardEntities;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        // save temporary CreditCardsEntities
        temporalStorageCreditCardEntities = fetchCreditCardAccounts();

        return temporalStorageCreditCardEntities.stream()
                .map(CreditCardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    public List<CreditCardEntity> fetchCreditCardAccounts() {
        List<CreditCardEntity> creditCardEntities = new ArrayList<>();
        // get the cards in global position if there is anyone
        String[] creditCardsData =
                apiClient.getGlobalPositionHtml().split("(?=name=\"form_tarjmp)");

        for (String data : creditCardsData) {
            Document dataContainer = Jsoup.parse("<form " + data, "", Parser.xmlParser());

            boolean isCreditCard =
                    dataContainer
                            .getElementsByAttributeValue("name", "DESCR_TIPOTAR")
                            .attr("value")
                            .contains("CREDITO");

            if (isCreditCard) {
                creditCardEntities.add(parseCreditCard(dataContainer));
            }
        }
        return creditCardEntities;
    }

    private CreditCardEntity parseCreditCard(Document dataContainer) {
        String cardNumb =
                dataContainer.getElementsByAttributeValue("name", "numeroTarjeta").attr("value");
        CreditCardEntity.CreditCardEntityBuilder creditCardBuilder = CreditCardEntity.builder();

        Elements cardDetails = dataContainer.select("td.totlistaI, td.totimplista");
        String maskedCardNumb = cardDetails.get(0).ownText().trim();
        String description = cardDetails.get(1).ownText();

        String disposed = cardDetails.get(2).ownText();
        disposed = disposed.equals("-") ? "0,00" : disposed;

        String limit = cardDetails.get(3).ownText();
        limit = limit.equals("-") ? "0,00" : limit;

        String available = cardDetails.get(4).ownText();
        available = available.equals("-") ? "0,00" : available;

        creditCardBuilder
                .maskedCardNumber(maskedCardNumb)
                .cardNumber(cardNumb)
                .disposed(parseAmountInEuros(disposed))
                .limit(parseAmountInEuros(limit))
                .available(parseAmountInEuros(available))
                .description(description);

        // fields necessary for next requests
        Elements inputs =
                dataContainer
                        .getElementsByTag("form")
                        .first()
                        .nextElementSiblings()
                        .select(Tags.TAG_INPUT);
        creditCardBuilder
                .cardCode(inputs.select(CssSelectors.CSS_CARD_CODE).attr(Tags.ATTRIBUTE_TAG_VALUE))
                .cardtype(inputs.select(CssSelectors.CSS_CARD_TYPE).attr(Tags.ATTRIBUTE_TAG_VALUE))
                .codeCardType(
                        inputs.select(CssSelectors.CSS_CODE_CARD_TYPE)
                                .attr(Tags.ATTRIBUTE_TAG_VALUE))
                .entityCard(
                        inputs.select(CssSelectors.CSS_ENTITY_CARD).attr(Tags.ATTRIBUTE_TAG_VALUE))
                .agreementCard(
                        inputs.select(CssSelectors.CSS_AGREEMENT_CARD)
                                .attr(Tags.ATTRIBUTE_TAG_VALUE))
                .panDescription(
                        inputs.select(CssSelectors.CSS_DESCRIPTION_PAN)
                                .attr(Tags.ATTRIBUTE_TAG_VALUE))
                .descriptionCardType(
                        inputs.select(CssSelectors.CSS_DESCRIPTION_CARD_TYPE)
                                .attr(Tags.ATTRIBUTE_TAG_VALUE));

        return creditCardBuilder.build();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {

        globalPosition = new GlobalPositionResponse(apiClient.getGlobalPositionHtml());
        PaginatorResponse response = PaginatorResponseImpl.createEmpty();

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusMonths(3);
        CreditCardEntity creditCardEntity =
                getCreditCardAccountMatchWithTemporalStorageEntities(account);

        if (creditCardEntity == null) {
            log.warn("WARN: CreditCard not found when fetch transactions");
            return response;
        }

        String transactionHtmlPage = getTransactionsByPeriod(creditCardEntity, fromDate, toDate);

        return parseTransactions(transactionHtmlPage, response);
    }

    private PaginatorResponse parseTransactions(
            String transactionHtmlPage, PaginatorResponse response) {

        if (transactionHtmlPage.contains(THERE_IS_NOT_DATA_FOR_THIS_CONSULT)
                || transactionHtmlPage.contains(INVALID_PERIOD)) {
            log.warn("WARN: NO exist transactions for the current period");
            return response;
        }

        // TODO implement when there are available transactions

        return response;
    }

    private CreditCardEntity getCreditCardAccountMatchWithTemporalStorageEntities(
            CreditCardAccount account) {
        return temporalStorageCreditCardEntities.stream()
                .filter(
                        accountEntity ->
                                accountEntity.getCardNumber().equals(account.getAccountNumber()))
                .findFirst()
                .orElse(null);
    }

    private String getTransactionsByPeriod(
            CreditCardEntity card, LocalDate fromDate, LocalDate toDate) {

        log.info("Go to Credit Cards tab");
        URL url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + globalPosition
                                        .getHtml()
                                        .getElementById("menuHorizontal")
                                        .select("a:containsOwn(Tarjetas)")
                                        .attr("href"));

        Document html = Jsoup.parse(apiClient.navigateToCreditCardsMovements(url));

        log.info("Go to Credit Cards movements");
        apiClient.setHeaderReferer(url.toString());
        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + html.select("a:containsOwn(Movimientos)").attr("href"));
        html = Jsoup.parse(apiClient.navigateToCreditCardsMovements(url));

        card.setAccountCode(extractCreditCardAccountCode(card, html));

        log.info("Select a Credit Card and go to transactions by dates");
        apiClient.setHeaderReferer(url.toString());
        url = URL.of(Urls.RURALVIA_SECURE_HOST + html.select("form").attr("action"));
        String params = generateFormBodyForCreditCardsMovements(card);
        html = Jsoup.parse(apiClient.navigateToCreditCardTransactionsByDates(url, params));

        log.info("request transactions between dates for specific credit card");
        url = URL.of(Urls.RURALVIA_SECURE_HOST + html.select("form").attr("action"));
        params = generateFormBodyForTransactionsBetweenDates(card, fromDate, toDate);

        return apiClient.requestTransactionsBetweenDates(url, params);
    }

    private String generateFormBodyForTransactionsBetweenDates(
            CreditCardEntity card, LocalDate fromDate, LocalDate toDate) {
        String sixMonthsDate = fromDate.minusMonths(6).format(PATTERN);
        return Form.builder()
                .put("ISUM_OLD_METHOD", "POST")
                .put("ISUM_ISFORM", "true")
                .put("FECHAMOVDESDE", fromDate.format(PATTERN))
                .put("FECHAMOVHASTA", toDate.format(PATTERN))
                .put("primeraVez", "1")
                .put("paginaActual", "0")
                .put("tamanioPagina", "50") // originally is 25
                .put("campoPaginacion", "lista")
                .put("clavePagina", "BEL_TAR_CON_MOVS_DATOS_SAT")
                .put("opcionSaldoUltMovs", "M")
                .put("numeroTarjeta", card.getCardNumber())
                .put(ParamValues.CARD_CODE, "")
                .put("tipoTarjeta_desc", getURLEncodedUTF8String(card.getDescriptionCardType()))
                .put(ParamValues.CARD_TYPE, card.getCardtype())
                .put(ParamValues.AGREEMENT_CARD, card.getAgreementCard())
                .put(ParamValues.ENTITY_CARD, card.getEntityCard())
                .put("TIPODOCUMENTO", "")
                .put("DOCUMENTO", "")
                .put("fechaseisMeses", sixMonthsDate)
                .put("clavePaginaVolver", "BEL_TAR_CON_MOVS_FECHAS_SAT")
                .put("volverNFU", "")
                .put("PAN", card.getCardNumber())
                .put(ParamValues.DESCRIPTION_PAN, getURLEncodedUTF8String(card.getPanDescription()))
                .put(
                        ParamValues.DESCRIPTION_CARD_TYPE,
                        getURLEncodedUTF8String(card.getDescriptionCardType()))
                .put("ACUERDO", card.getAgreementCard())
                .put("IRIS", "SI")
                .put(ParamValues.CODE_CARD_TYPE, card.getCodeCardType())
                .build()
                .serialize();
    }

    private String extractCreditCardAccountCode(CreditCardEntity card, Document html) {
        // <A ... onClick="javaScript:validar('4**************1','01_51', ... </A>
        String onClickData =
                html.getElementById("BODY_LISTA")
                        .select("A[onClick*=" + card.getCardNumber() + "]")
                        .attr("onClick");
        int start = onClickData.indexOf(",'") + 2; // +2 to avoid extract the chars ",'"
        int end = onClickData.indexOf("',", start);
        return onClickData.substring(start, end);
    }

    private String generateFormBodyForCreditCardsMovements(CreditCardEntity card) {
        return Form.builder()
                .put("ISUM_OLD_METHOD", "get")
                .put("ISUM_ISFORM", "true")
                .put("primeraVez", "0")
                .put("paginaActual", "0")
                .put("tamanioPagina", "50") // by default should be 25
                .put("campoPaginacion", "lista")
                .put("pagXpag", "")
                .put("clavePagina", "BEL_TAR_CON_MOVS_FECHAS_SAT")
                .put("numeroTarjeta", card.getCardNumber())
                .put(ParamValues.CARD_TYPE, card.getCardtype())
                .put(
                        ParamValues.DESCRIPTION_CARD_TYPE,
                        getURLEncodedUTF8String(card.getDescriptionCardType()))
                .put("codigoCuenta", card.getAccountCode())
                .put(ParamValues.ENTITY_CARD, card.getEntityCard())
                .put(ParamValues.AGREEMENT_CARD, card.getAgreementCard())
                .put("PAN", card.getCardNumber())
                .put("ACUERDO", card.getAgreementCard())
                .put(ParamValues.DESCRIPTION_PAN, getURLEncodedUTF8String(card.getPanDescription()))
                .put(
                        ParamValues.DESCRIPTION_CARD_TYPE,
                        getURLEncodedUTF8String(card.getDescriptionCardType()))
                .put("clavePaginaVolver", "BEL_TAR_CON_MOVS_SAT")
                .put("opcionSaldoUltMovs", "M")
                .put("IRIS", "SI")
                .put(ParamValues.CODE_CARD_TYPE, card.getCodeCardType())
                .build()
                .serialize();
    }
}
