package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LOCAL_DATE_PATTERN;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.ACCOUNT_DESCRIPTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.CURRENT_PAGE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.FIRST_TIME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGE_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.PAGINATION_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues.SELECTED_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_ACTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_HREF;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_NAME;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls.RURALVIA_SECURE_HOST;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.exceptions.refresh.LoanAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.ParamValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities.LoanEntity.LoanEntityBuilder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.form.Form.Builder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class RuralviaLoanFetcher implements AccountFetcher<LoanAccount> {

    private final RuralviaApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return fetchLoanAccounts().stream()
                .map(LoanEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }

    public List<LoanEntity> fetchLoanAccounts() {
        List<LoanEntity> loans = new ArrayList<>();

        GlobalPositionResponse globalPosition =
                new GlobalPositionResponse(apiClient.getGlobalPositionHtml());

        Elements loansContainer =
                globalPosition
                        .getHtml()
                        .select("div[id=HEADER_XA]:has(div:contains(Financiaci)) + div");

        Elements loansForm = loansContainer.select("form ~ tr:has(td[class=totlistaC])");

        for (Element loan : loansForm) {
            LoanEntityBuilder loanBuilder = getLoanBasicInfoFromGlobalPosition(loan);
            String html = navigateToLoanDetails(loanBuilder);
            addLoanDetails(Jsoup.parse(html).getElementById("PORTLET-DATO"), loanBuilder);
            html = navigateToAmortizationTable(html, loanBuilder);
            loans.add(parseAmortizationTableDetails(html, loanBuilder).build());
        }

        return loans;
    }

    private LoanEntityBuilder parseAmortizationTableDetails(
            String html, LoanEntityBuilder loanBuilder) {
        if (html.contains("En este momento, no es posible realizar la operaci")) {
            throw new LoanAccountRefreshException(
                    "At this time, it is not possible to perform the requested operation");
        }
        Element doc = Jsoup.parse(html);

        Elements nextMonthAmortizationRow =
                doc.getElementById("BODY_LISTA").select("tr").get(1).getElementsByTag("td");

        String deductionDate = nextMonthAmortizationRow.get(0).ownText();
        String monthlyAmortization = nextMonthAmortizationRow.get(1).ownText();

        loanBuilder.deductionDate(deductionDate).monthlyAmortization(monthlyAmortization);

        return loanBuilder;
    }

    private String navigateToAmortizationTable(String html, LoanEntityBuilder loanBuilder) {
        Element doc = Jsoup.parse(html);
        URL nextUrl =
                URL.of(
                        RURALVIA_SECURE_HOST
                                + doc.select("a:containsOwn(Cuadro de amortizaci)")
                                        .get(0)
                                        .attr(ATTRIBUTE_TAG_HREF));
        html = apiClient.navigateThroughLoan(nextUrl);

        LoanEntity loan = loanBuilder.build();
        doc = Jsoup.parse(html);
        doc.select("option:containsOwn(" + loan.getAccountNumber() + ")").attr(ATTRIBUTE_TAG_VALUE);
        URL url =
                URL.of(
                        RURALVIA_SECURE_HOST
                                + doc.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, "FORM_RVIA_0")
                                        .get(0)
                                        .attr(ATTRIBUTE_TAG_ACTION));
        String params = extractAmortitzationTableRequestParams(loan, doc);

        return apiClient.navigateToLoanAmortizationTableDetails(url, params);
    }

    private String extractAmortitzationTableRequestParams(LoanEntity loan, Element html) {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Madrid"));
        String accountNumber = loan.getAccountNumber().replaceAll("[\\s\\u202F\\u00A0]", "");
        String toDate = today.plusYears(1).format(LOCAL_DATE_PATTERN);
        String todayFormatted = today.format(LOCAL_DATE_PATTERN);

        String token = html.getElementById("tokenValid").attr("data-token");

        return Form.builder()
                .put("ISUM_OLD_METHOD", "POST")
                .put("ISUM_ISFORM", "true")
                .put(SELECTED_ACCOUNT, accountNumber + loan.getDescription())
                .put("FECHAMOVDESDE", todayFormatted)
                .put("FECHAMOVHASTA", toDate)
                .put(PAGE_KEY, "PRE_AMORTIZACION")
                .put(FIRST_TIME, "1")
                .put(CURRENT_PAGE, "0")
                .put(PAGE_SIZE, "25")
                .put(PAGINATION_FIELD, "lista")
                .put(ACCOUNT, accountNumber)
                .put(ACCOUNT_DESCRIPTION, loan.getDescription())
                .put(ParamValues.FROM_DATE, todayFormatted)
                .put(ParamValues.TO_DATE, toDate)
                .put("fechaActual", todayFormatted)
                .put("TRANCODE", "")
                .put("validationToken", token)
                .build()
                .serialize();
    }

    private LoanEntityBuilder getLoanBasicInfoFromGlobalPosition(Element loan) {
        Elements accountData = loan.select("td");
        String accountNumber = accountData.get(0).ownText();
        String description = accountData.get(1).ownText();
        String currentBalance = accountData.get(2).ownText(); // debt
        String initialBalance = accountData.get(3).ownText();
        Type loanType = description.toUpperCase().contains("HIP") ? Type.MORTGAGE : Type.CREDIT;

        return LoanEntity.builder()
                .accountNumber(accountNumber)
                .description(description)
                .currentBalance(currentBalance)
                .initialBalance(initialBalance)
                .accountType(loanType)
                .build()
                .toBuilder();
    }

    private void addLoanDetails(Element dataContainer, LoanEntityBuilder loanBuilder) {
        extractValue(dataContainer, () -> "td:containsOwn(titular) + td")
                .ifPresent(loanBuilder::applicant);
        extractValue(dataContainer, () -> "td:containsOwn(Tipo de inter) + td")
                .map(s -> s.replace("\"", ""))
                .ifPresent(loanBuilder::interestRate);
        extractValue(dataContainer, () -> "td:containsOwn(Fecha Formalizaci) + td")
                .ifPresent(loanBuilder::startDate);
        extractValue(dataContainer, () -> "td:containsOwn(Fecha Vencimiento) + td")
                .ifPresent(loanBuilder::endDate);
        extractValue(dataContainer, () -> "td:containsOwn(Capital Amortizado) + td")
                .ifPresent(loanBuilder::amortizedAmount);
    }

    private static Optional<String> extractValue(Element source, Supplier<String> query) {
        return Optional.ofNullable(source.select(query.get()))
                .filter(elements -> !elements.isEmpty())
                .map(elements -> elements.get(0).ownText());
    }

    private String navigateToLoanDetails(LoanEntity.LoanEntityBuilder loanBuilder) {
        URL url =
                URL.of(
                        RURALVIA_SECURE_HOST
                                + "/isum/srv.BDP_RVIA05_PRESTAMOFINAN_PAR.BDP_RVIA05_FINAN_PRESTAMOS_SITUAC");

        String html = apiClient.navigateThroughLoan(url);

        // if there is just one loan is returned here
        if (html.contains(">Datos del Préstamo<")) {
            return html;
        }

        Element form = Jsoup.parse(html).select("FORM").first();
        url = URL.of(RURALVIA_SECURE_HOST + form.attr("action"));

        String params = extractParamsForDetailsRequest(html, form, loanBuilder.build());

        html = apiClient.navigateToLoanDetails(url, params);

        return html;
    }

    private String extractParamsForDetailsRequest(String html, Element form, LoanEntity loan) {
        Elements optionSelector = form.select("select > option");
        String selectedAccount;
        if (optionSelector.isEmpty()) {
            selectedAccount =
                    form.getElementsByAttributeValue(ATTRIBUTE_TAG_NAME, ACCOUNT)
                            .attr(ATTRIBUTE_TAG_VALUE);
        } else {
            selectedAccount =
                    optionSelector
                            .select("option:containsOwn(" + loan.getAccountNumber() + ")")
                            .attr(ATTRIBUTE_TAG_VALUE);
        }

        String tranCode =
                RuralviaUtils.extractToken(html, "FORM_RVIA_0.TRANCODE.value =", "\"", "\";");

        Builder formBuilder = getParamsFromInputTags(form, loan, selectedAccount, tranCode);
        formBuilder.put(
                "validationToken",
                Jsoup.parse(html).getElementById("tokenValid").attr("data-token"));

        return formBuilder.build().serialize();
    }

    private Builder getParamsFromInputTags(
            Element form, LoanEntity loan, String selectedAccount, String tranCode) {
        Elements inputs = form.select("INPUT");
        Builder formBuilder = Form.builder();
        for (Element input : inputs) {
            String name;
            String value;

            name = input.attr(ATTRIBUTE_TAG_NAME);
            switch (name) {
                case PAGE_KEY:
                    value = "PRE_CONSULTA";
                    break;
                case PAGINATION_FIELD:
                    value = "lista";
                    break;
                case PAGE_SIZE:
                    value = "10";
                    break;
                case "TRANCODE":
                    value = tranCode;
                    break;
                case SELECTED_ACCOUNT:
                    value = selectedAccount;
                    break;
                case ACCOUNT_DESCRIPTION:
                    value = loan.getDescription();
                    break;
                case ACCOUNT:
                    value = loan.getAccountNumber().replaceAll("[\\s\\u202F\\u00A0]", "");
                    break;
                case FIRST_TIME:
                    value = "1";
                    break;
                case CURRENT_PAGE:
                    value = "0";
                    break;
                default:
                    value = input.attr(ATTRIBUTE_TAG_VALUE);
            }
            formBuilder.put(name, value);
        }
        return formBuilder.put(SELECTED_ACCOUNT, selectedAccount);
    }
}
