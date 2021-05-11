package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.creditcard;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.CssSelectors;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.INVALID_PERIOD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LOCAL_DATE_PATTERN;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.CURRENT_PAGE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.DESCRIPTION_CARD_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FIRST_TIME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGINATION_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.RETURN_PAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.THERE_IS_NOT_DATA_FOR_THIS_CONSULT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_HREF;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
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
        temporalStorageCreditCardEntities = fetchCreditCardAccounts();

        return temporalStorageCreditCardEntities.stream()
                .map(CreditCardEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    public List<CreditCardEntity> fetchCreditCardAccounts() {
        List<CreditCardEntity> creditCardEntities = new ArrayList<>();
        String[] creditCardsData =
                apiClient.getGlobalPositionHtml().split("(?=name=\"form_tarjmp)");

        for (String data : creditCardsData) {
            Document dataContainer = Jsoup.parse("<form " + data, "", Parser.xmlParser());

            boolean isCreditCard =
                    dataContainer
                            .getElementsByAttributeValue(
                                    Tags.ATTRIBUTE_TAG_NAME, DESCRIPTION_CARD_TYPE)
                            .attr(Tags.ATTRIBUTE_TAG_VALUE)
                            .contains("CREDITO");

            if (isCreditCard) {
                creditCardEntities.add(parseCreditCard(dataContainer));
            }
        }
        return creditCardEntities;
    }

    private CreditCardEntity parseCreditCard(Document dataContainer) {
        String cardNumb =
                dataContainer
                        .getElementsByAttributeValue(
                                Tags.ATTRIBUTE_TAG_NAME, ParamValues.CARD_NUMBER)
                        .attr(Tags.ATTRIBUTE_TAG_VALUE);
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
                        .getElementsByTag(Tags.FORM_TAG)
                        .first()
                        .nextElementSiblings()
                        .select(Tags.INPUT_TAG);
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
        PaginatorResponse response = PaginatorResponseImpl.createEmpty(false);

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
                                        .attr(ATTRIBUTE_TAG_HREF));

        Document html = Jsoup.parse(apiClient.navigateToCreditCardsMovements(url));

        log.info("Go to Credit Cards movements");
        apiClient.setHeaderReferer(url.toString());
        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + html.select("a:containsOwn(Movimientos)")
                                        .attr(ATTRIBUTE_TAG_HREF));
        html = Jsoup.parse(apiClient.navigateToCreditCardsMovements(url));

        card.setAccountCode(extractCreditCardAccountCode(card, html));

        log.info("Select a Credit Card and go to transactions by dates");
        apiClient.setHeaderReferer(url.toString());
        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + html.select("form").attr(Tags.ATTRIBUTE_TAG_ACTION));
        String params = generateFormBodyForCreditCardsMovements(card);
        html = Jsoup.parse(apiClient.navigateToCreditCardTransactionsByDates(url, params));

        log.info("request transactions between dates for specific credit card");
        url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + html.select("form").attr(Tags.ATTRIBUTE_TAG_ACTION));
        params = generateFormBodyForTransactionsBetweenDates(card, fromDate, toDate);

        return apiClient.requestTransactionsBetweenDates(url, params);
    }

    private String generateFormBodyForTransactionsBetweenDates(
            CreditCardEntity card, LocalDate fromDate, LocalDate toDate) {
        String sixMonthsDate = fromDate.minusMonths(6).format(LOCAL_DATE_PATTERN);
        return Form.builder()
                .put("ISUM_OLD_METHOD", "POST")
                .put("ISUM_ISFORM", "true")
                .put("FECHAMOVDESDE", fromDate.format(LOCAL_DATE_PATTERN))
                .put("FECHAMOVHASTA", toDate.format(LOCAL_DATE_PATTERN))
                .put(FIRST_TIME, "1")
                .put(CURRENT_PAGE, "0")
                .put(PAGE_SIZE, "50") // originally is 25
                .put(PAGINATION_FIELD, "lista")
                .put(PAGE_KEY, "BEL_TAR_CON_MOVS_DATOS_SAT")
                .put("opcionSaldoUltMovs", "M")
                .put(ParamValues.CARD_NUMBER, card.getCardNumber())
                .put(ParamValues.CARD_CODE, "")
                .put("tipoTarjeta_desc", card.getDescriptionCardType())
                .put(ParamValues.CARD_TYPE, card.getCardtype())
                .put(ParamValues.AGREEMENT_CARD, card.getAgreementCard())
                .put(ParamValues.ENTITY_CARD, card.getEntityCard())
                .put("TIPODOCUMENTO", "")
                .put("DOCUMENTO", "")
                .put("fechaseisMeses", sixMonthsDate)
                .put(RETURN_PAGE_KEY, "BEL_TAR_CON_MOVS_FECHAS_SAT")
                .put("volverNFU", "")
                .put("PAN", card.getCardNumber())
                .put(ParamValues.DESCRIPTION_PAN, card.getPanDescription())
                .put(DESCRIPTION_CARD_TYPE, card.getDescriptionCardType())
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
                .put(FIRST_TIME, "0")
                .put(CURRENT_PAGE, "0")
                .put(PAGE_SIZE, "50") // by default should be 25
                .put(PAGINATION_FIELD, "lista")
                .put("pagXpag", "")
                .put(PAGE_KEY, "BEL_TAR_CON_MOVS_FECHAS_SAT")
                .put(ParamValues.CARD_NUMBER, card.getCardNumber())
                .put(ParamValues.CARD_TYPE, card.getCardtype())
                .put(DESCRIPTION_CARD_TYPE, card.getDescriptionCardType())
                .put("codigoCuenta", card.getAccountCode())
                .put(ParamValues.ENTITY_CARD, card.getEntityCard())
                .put(ParamValues.AGREEMENT_CARD, card.getAgreementCard())
                .put("PAN", card.getCardNumber())
                .put("ACUERDO", card.getAgreementCard())
                .put(ParamValues.DESCRIPTION_PAN, card.getPanDescription())
                .put(DESCRIPTION_CARD_TYPE, card.getDescriptionCardType())
                .put(RETURN_PAGE_KEY, "BEL_TAR_CON_MOVS_SAT")
                .put("opcionSaldoUltMovs", "M")
                .put("IRIS", "SI")
                .put(ParamValues.CODE_CARD_TYPE, card.getCodeCardType())
                .build()
                .serialize();
    }
}
