package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.PATTERN;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags.ATTRIBUTE_TAG_ACTION;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls.RURALVIA_SECURE_HOST;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities.LoanEntity.LoanEntityBuilder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class RuralviaLoanFetcher implements AccountFetcher<LoanAccount> {

    private final RuralviaApiClient apiClient;
    private List<LoanEntity> temporalStorageLoans;

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        temporalStorageLoans = fetchLoanAccounts();

        return temporalStorageLoans.stream()
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

        // parse basic info for loan
        Elements loansForm = loansContainer.select("form ~ tr:has(td[class=totlistaC])");

        for (Element loan : loansForm) {

            LoanEntityBuilder loanBuilder = getLoanBasicInfoFromGlobalPosition(loan);

            String html = navigateToLoanDetails(loanBuilder);

            loanBuilder = extractDetails(html, loanBuilder);

            html = navigateToAmortizationTable(html, loanBuilder);

            loanBuilder = parseAmortizationTableDetails(html, loanBuilder);

            loans.add(loanBuilder.build());
        }

        return loans;
    }

    private LoanEntityBuilder parseAmortizationTableDetails(
            String html, LoanEntityBuilder loanBuilder) {
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
                                        .attr("href"));
        html = apiClient.navigateThroughLoan(nextUrl);

        LoanEntity loan = loanBuilder.build();
        doc = Jsoup.parse(html);
        doc.select("option:containsOwn(" + loan.getAccountNumber() + ")").attr("value");
        URL url =
                URL.of(
                        RURALVIA_SECURE_HOST
                                + doc.getElementsByAttributeValue("name", "FORM_RVIA_0")
                                        .get(0)
                                        .attr(ATTRIBUTE_TAG_ACTION));
        String params = extractAmortitzationTableRequestParams(loan);

        html = apiClient.navigateToLoanAmortizationTableDetails(url, params);

        return html;
    }

    private String extractAmortitzationTableRequestParams(LoanEntity loan) {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Madrid"));
        String accountNumber = loan.getAccountNumber().replaceAll("[\\s\\u202F\\u00A0]", "");
        String toDate = today.plusYears(1).format(PATTERN);
        String todayFormatted = today.format(PATTERN);

        return Form.builder()
                .put("ISUM_OLD_METHOD", "POST")
                .put("ISUM_ISFORM", "true")
                .put("SELCTA", accountNumber + loan.getDescription())
                .put("FECHAMOVDESDE", todayFormatted)
                .put("FECHAMOVHASTA", toDate)
                .put("clavePagina", "PRE_AMORTIZACION")
                .put("primeraVez", "1")
                .put("paginaActual", "0")
                .put("tamanioPagina", "25")
                .put("campoPaginacion", "lista")
                .put("cuenta", accountNumber)
                .put("descripcionCuenta", loan.getDescription())
                .put("fechaDesde", todayFormatted)
                .put("fechaHasta", toDate)
                .put("fechaActual", todayFormatted)
                .put("TRANCODE", "")
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

    private LoanEntityBuilder extractDetails(String html, LoanEntityBuilder loanBuilder) {

        Element dataContainer = Jsoup.parse(html).getElementById("PORTLET-DATO");
        String loanHolder = dataContainer.select("td:containsOwn(titular) + td").get(0).ownText();
        String interesRate =
                dataContainer.select("td:containsOwn(Tipo de inter) + td").get(0).ownText();
        String startDate =
                dataContainer.select("td:containsOwn(Fecha Formalizaci) + td").get(0).ownText();
        String amortizedAmount =
                dataContainer.select("td:containsOwn(Capital Amortizado) + td").get(0).ownText();
        String endDate =
                dataContainer.select("td:containsOwn(Fecha Vencimiento) + td").get(0).ownText();

        return loanBuilder
                .applicant(loanHolder)
                .interestRate(interesRate)
                .startDate(startDate)
                .amortizedAmount(amortizedAmount)
                .endDate(endDate);
    }

    public String navigateToLoanDetails(LoanEntity.LoanEntityBuilder loanBuilder) {

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
            selectedAccount = form.getElementsByAttributeValue("name", "cuenta").attr("value");
        } else {
            selectedAccount =
                    optionSelector
                            .select("option:containsOwn(" + loan.getAccountNumber() + ")")
                            .attr("value");
        }

        int beginIndex = html.indexOf("FORM_RVIA_0.TRANCODE.value =");
        beginIndex = html.indexOf("\"", beginIndex) + 1;
        int endIndex = html.indexOf("\";", beginIndex);
        String tranCode = html.substring(beginIndex, endIndex);

        Elements inputs = form.select("INPUT");
        Form.Builder formBuilder = Form.builder();
        for (Element input : inputs) {
            String name;
            String value;

            name = input.attr("name");
            value = input.attr("value");
            //
            if (name.equals("clavePagina")) {
                value = "PRE_CONSULTA";
            } else if (name.equals("campoPaginacion")) {
                value = "lista";
            } else if (name.equals("tamanioPagina")) {
                value = "10";
            } else if (name.equals("TRANCODE")) {
                value = tranCode;
            } else if (name.equals("SELCTA")) {
                value = selectedAccount;
            } else if (name.equals("descripcionCuenta")) {
                value = loan.getDescription();
            } else if (name.equals("cuenta")) {
                value = loan.getAccountNumber().replaceAll("[\\s\\u202F\\u00A0]", "");
            } else if (name.equals("primeraVez")) {
                value = "1";
            } else if (name.equals("paginaActual")) {
                value = "0";
            }

            formBuilder.put(name, value);
        }
        formBuilder
                .put("SELCTA", selectedAccount)
                .put(
                        "validationToken",
                        Jsoup.parse(html).getElementById("tokenValid").attr("data-token"));

        return formBuilder.build().serialize();
    }
}
