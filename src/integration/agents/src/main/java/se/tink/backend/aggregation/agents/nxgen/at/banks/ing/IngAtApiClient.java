package se.tink.backend.aggregation.agents.nxgen.at.banks.ing;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.credit.rpc.CreditCardTransactionPage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional.rpc.CSVTransactionsPage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtAntiCacheParser;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtOpeningDateParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public final class IngAtApiClient {
    private static final Logger logger = LoggerFactory.getLogger(IngAtApiClient.class);
    private final IngAtSessionStorage ingAtSessionStorage;
    private final TinkHttpClient client;

    // Increments every time a CSV file is downloaded
    private int downloadCount = 1;

    // TODO Should be used everywhere and maybe encapsulated in an immutable State class
    private int currentPage = 0;

    // TODO session storage?
    // Account numbers -> account opening dates
    private final Map<String, Date> accountOpeningDates = new HashMap<>();

    public IngAtApiClient(
            final TinkHttpClient client,
            final Provider provider,
            IngAtSessionStorage ingAtSessionStorage) {
        this.client = client;
        this.ingAtSessionStorage = ingAtSessionStorage;
    }

    private RequestBuilder commonHeaders(final URL url) {
        return client.request(url)
                .header("Host", "banking.ing.at")
                .header("Accept", "application/xml, text/xml, */*; q=0.01")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Connection", "keep-alive");
    }

    public static URL getLastRedirectUrl(final List<URI> redirects) {
        if (redirects.isEmpty()) {
            throw new IllegalStateException("Failed to pickup redirects");
        } else {
            return new URL(redirects.get(redirects.size() - 1).toString());
        }
    }

    public static URL getLastRedirectUrl(final HttpResponse response) {
        return getLastRedirectUrl(response.getRedirects());
    }

    private void updateCurrentUrl(final HttpResponse response, final URL defaultUrlIfNoRedirects) {
        final List<URI> redirects = response.getRedirects();
        if (redirects.isEmpty()) {
            ingAtSessionStorage.setCurrentUrl(defaultUrlIfNoRedirects.toString());
        } else {
            final String lastRedirectUrl = redirects.get(redirects.size() - 1).toString();
            ingAtSessionStorage.setCurrentUrl(lastRedirectUrl);
        }
    }

    public HttpResponse initiateWebLogin(URL url) {
        final HttpResponse r = client.request(url).get(HttpResponse.class);
        updateCurrentUrl(r, url);
        return r;
    }

    public HttpResponse logIn(URL url, Form passwordForm) {
        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Origin", "https://banking.ing.at")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header(
                                "Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Referer", "https://banking.ing.at/online-banking/wicket/login?0")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7")
                        .body(passwordForm.serialize())
                        .post(HttpResponse.class);

        updateCurrentUrl(response, url);
        return response;
    }

    public HttpResponse search(URL url, Form form) {
        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Connection", "keep-alive")
                        .header("Origin", "https://banking.ing.at")
                        .header("Wicket-FocusedElementId", "id172")
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .header("Accept", "application/xml, text/xml, */*; q=0.01")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Wicket-Ajax", "true")
                        .header("Wicket-Ajax-BaseURL", "wicket/page?2")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?2")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7")
                        .body(form.serialize())
                        .post(HttpResponse.class);

        updateCurrentUrl(response, url);
        return response;
    }

    public HttpResponse getAccountDetails(URL url) {
        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header(
                                "Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?0")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7")
                        .get(HttpResponse.class);

        updateCurrentUrl(response, url);
        return response;
    }

    public void logOut() {
        try {
            final HttpResponse response =
                    client.request(IngAtConstants.Url.LOGOUT)
                            .header("Host", "banking.ing.at")
                            .header("Upgrade-Insecure-Requests", "1")
                            .header(
                                    "Accept",
                                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Encoding", "gzip, deflate, br")
                            .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8,sv;q=0.7")
                            .get(HttpResponse.class);
            updateCurrentUrl(response, IngAtConstants.Url.LOGOUT);
        } catch (HttpResponseException e) {
            logger.warn("Failed to log out: {}", e);
        }
    }

    private static Date today(int shiftedDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, shiftedDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date dateWithoutTime(final Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date adjustDate(Date d) {
        final Date today = today(0);
        if (d.compareTo(today) >= 0) {
            return today(-1); // yesterday
        } else {
            return dateWithoutTime(d);
        }
    }

    private static Date adjustDate(Date d, final Date lowerBound) {
        final Date d2 = adjustDate(d);
        if (d2.compareTo(lowerBound) <= 0) {
            return lowerBound;
        }
        return d2;
    }

    private static int pageNumberFromAjaxLocation(final String s) {
        final Pattern pattern = Pattern.compile("/page\\?([0-9]+)");
        final Matcher m = pattern.matcher(s);
        final boolean wasFound = m.find();
        if (!wasFound) {
            throw new IllegalStateException();
        }
        return Integer.parseInt(m.group(1));
    }

    private static Form dateRangesForm(final Date fromDate, final Date toDate) {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        return Form.builder()
                .put("dateRange:ranges", "radio24")
                .put(
                        "dateRange:ranges:individuellVonBorder:individuellVonBorder_body:individuellVon",
                        dateFormatter.format(fromDate))
                .put(
                        "dateRange:ranges:individuellBisBorder:individuellBisBorder_body:individuellBis",
                        dateFormatter.format(toDate))
                .put("dateRange:ranges:monate", "0")
                .put("dateRange:ranges:quartale", "0")
                .put("dateRange:ranges:jahre", "0")
                .build();
    }

    private static Form downloadForm(Date fromDate, Date toDate) {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

        return Form.builder()
                .put("id16e_hf_0", "")
                .put("exportType", "radio16")
                .put("dateRange:ranges:monate", "0")
                .put("dateRange:ranges:quartale", "0")
                .put("dateRange:ranges:jahre", "0")
                .put("dateRange:ranges", "radio24")
                .put(
                        "dateRange:ranges:individuellVonBorder:individuellVonBorder_body:individuellVon",
                        dateFormatter.format(fromDate))
                .put(
                        "dateRange:ranges:individuellBisBorder:individuellBisBorder_body:individuellBis",
                        dateFormatter.format(toDate))
                .put("download", "1")
                .build();
    }

    private HttpResponse redirect(final HttpResponse message) {
        final int pageNo =
                pageNumberFromAjaxLocation(message.getHeaders().getFirst("Ajax-Location"));

        final URL url =
                new URL("https://banking.ing.at/online-banking/wicket/wicket/page?" + pageNo);
        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Accept", "application/xml, text/xml, */*; q=0.01")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?1")
                        .header("Wicket-Ajax", "true")
                        .header("Wicket-Ajax-BaseURL", "wicket/page?" + pageNo)
                        .header("Wicket-FocusedElementId", "id49")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Connection", "keep-alive")
                        .get(HttpResponse.class);
        return response;
    }

    private HttpResponse requestFormDownload(
            final int pageNumber, final Date fromDate, final Date toDate) {
        final URL url =
                new URL(
                        "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                + pageNumber
                                + "-"
                                + downloadCount
                                + ".IBehaviorListener.0-form-download");
        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Accept", "application/xml, text/xml, */*; q=0.01")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                        + pageNumber)
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .header("Wicket-Ajax", "true")
                        .header("Wicket-Ajax-BaseURL", "wicket/page?" + pageNumber)
                        .header("Wicket-FocusedElementId", "id17e")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Connection", "keep-alive")
                        .body(downloadForm(fromDate, toDate).serialize())
                        .post(HttpResponse.class);

        return response;
    }

    // The protocol for downloading a CSV file with all transactions within a date range repeatedly
    // looks like this:
    //
    // GET
    // https://banking.ing.at/online-banking/wicket/wicket/page?1-1.IBehaviorListener.0-umsaetzePanel-suche-exportLink&_=<NUMBER1>
    // GET https://banking.ing.at/online-banking/wicket/wicket/page?2
    // POST
    // https://banking.ing.at/online-banking/wicket/wicket/page?2-1.IBehaviorListener.0-form-dateRange-ranges
    // POST
    // https://banking.ing.at/online-banking/wicket/wicket/page?2-1.IBehaviorListener.0-form-download
    // GET
    // https://banking.ing.at/online-banking/wicket/wicket/page?2-1.IBehaviorListener.0-&antiCache=<NUMBER2>
    // ^--- Response contains first CSV body
    //
    // GET https://banking.ing.at/online-banking/wicket/wicket/page?2
    // POST
    // https://banking.ing.at/online-banking/wicket/wicket/page?2-2.IBehaviorListener.0-form-download
    // GET
    // https://banking.ing.at/online-banking/wicket/wicket/page?2-2.IBehaviorListener.0-&antiCache=<NUMBER3>
    // ^--- Response contains second CSV body
    //
    // where <NUMBER1>, <NUMBER2>, <NUMBER3> are placeholders.

    public PaginatorResponse getTransactionsResponse(
            final TransactionalAccount account, final Date fromDate, final Date toDate) {

        switch (account.getType()) {
            case CHECKING:
                if (!accountOpeningDates.containsKey(account.getAccountNumber())) {
                    final Date openingDate = getAccountOpeningDate();
                    accountOpeningDates.put(account.getAccountNumber(), openingDate);
                }
                return getCheckingTransactionsResponse(account, fromDate, toDate);
            case SAVINGS:
                return getSavingsTransactionsResponse(account);
        }
        throw new IllegalStateException("Unexpected transaction type");
    }

    private static Date extractAccountOpeningDate(final String html) {
        final String dateString = new IngAtOpeningDateParser(html).getOpeningDate();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            return dateFormatter.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalStateException();
        }
    }

    private Date getAccountOpeningDate() {
        final HttpResponse exportResponse = requestExport();
        redirect(exportResponse);

        // Most certainly didn't have transactions from before Jesus was born
        final Date fromDate = new GregorianCalendar(1, 1, 1).getTime();
        // TODO tell my ancestors to update this by year 10000 AD
        final Date toDate = new GregorianCalendar(10000, 1, 1).getTime();

        requestDateRange(exportResponse, fromDate, toDate);
        currentPage =
                pageNumberFromAjaxLocation(exportResponse.getHeaders().getFirst("Ajax-Location"));
        final HttpResponse responseFormDownload =
                requestFormDownload(currentPage, fromDate, toDate);

        downloadCount += 1; // TODO

        return extractAccountOpeningDate(responseFormDownload.getBody(String.class));
    }

    public PaginatorResponse getTransactionsResponse(final CreditCardAccount account) {
        switch (account.getType()) {
            case CREDIT_CARD:
                return getCreditCardTransactionResponse(account);
        }
        throw new IllegalStateException("Unexpected transaction type");
    }

    private PaginatorResponse getCreditCardTransactionResponse(final CreditCardAccount account) {
        return new CreditCardTransactionPage(account);
    }

    private PaginatorResponse getCheckingTransactionsResponse(
            final TransactionalAccount account, final Date fromDate, final Date toDate) {

        refreshCurrentPage();

        final Date adjustedFromDate =
                adjustDate(fromDate, accountOpeningDates.get(account.getAccountNumber()));
        final Date adjustedToDate =
                adjustDate(toDate, accountOpeningDates.get(account.getAccountNumber()));

        final HttpResponse responseFormDownload =
                requestFormDownload(currentPage, adjustedFromDate, adjustedToDate);
        final HttpResponse antiCacheResponse = requestAntiCache(responseFormDownload);

        final Date openingDate = accountOpeningDates.get(account.getAccountNumber());
        final boolean isAfterOpeningDate = adjustedFromDate.compareTo(openingDate) > 0;

        final CSVTransactionsPage csvPaginatorResponse =
                new CSVTransactionsPage(
                        antiCacheResponse.getBody(String.class), isAfterOpeningDate);

        downloadCount += 1; // TODO

        return csvPaginatorResponse;
    }

    private void refreshCurrentPage() {
        final URL url =
                new URL("https://banking.ing.at/online-banking/wicket/wicket/page?" + currentPage);
        final HttpResponse response =
                commonHeaders(url)
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .get(HttpResponse.class);
    }

    private void requestExportButton(final HttpResponse message) {
        final String underscoreValue = "1544442615301"; // Copied from production logs
        final int pageNo = pageNumberFromAjaxLocation(message.getRedirects().get(0).toString());
        final URL url =
                new URL(
                        String.format(
                                "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                        + pageNo
                                        + "-1.IBehaviorListener.0-umsaetze-transactionsSearchbarContainer-exportButton&_=%s",
                                underscoreValue));
        commonHeaders(url)
                .header("Wicket-Ajax", "true")
                .header("Wicket-Ajax-BaseURL", "wicket/page?" + pageNo)
                .header("X-Requested-With", "XMLHttpRequest")
                .get(HttpResponse.class);
    }

    private HttpResponse requestExportPanel(final HttpResponse message) {
        final String underscoreValue = "1544442615302"; // Copied from production logs
        final int pageNo = pageNumberFromAjaxLocation(message.getRedirects().get(0).toString());
        final URL url =
                new URL(
                        String.format(
                                "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                        + pageNo
                                        + "-1.IBehaviorListener.0-umsaetze-transactionsSearchbarContainer-exportPanel-csvExportDesktop&_=%s",
                                underscoreValue));
        final HttpResponse response =
                commonHeaders(url)
                        .header("Wicket-Ajax", "true")
                        .header("Wicket-Ajax-BaseURL", "wicket/page?1")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get(HttpResponse.class);
        return response;
    }

    private HttpResponse selectAccount(final TransactionalAccount account, final int pageNo) {
        final int accountIndex =
                Integer.parseInt(
                        account.getFromTemporaryStorage(
                                IngAtConstants.Storage.ACCOUNT_INDEX.name()));
        final URL url =
                new URL(
                        "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                + pageNo
                                + "-1.ILinkListener-nav~wrapper-kontenAusklappen-kontoTableRepeater-"
                                + accountIndex
                                + "-kontoDetailLink");
        final HttpResponse response = commonHeaders(url).get(HttpResponse.class);
        return response;
    }

    private PaginatorResponse getSavingsTransactionsResponse(final TransactionalAccount account) {
        final int pageNo = 0;

        final HttpResponse selectAccountResponse = selectAccount(account, pageNo);
        requestExportButton(selectAccountResponse);
        final HttpResponse responseExportPanel = requestExportPanel(selectAccountResponse);
        final HttpResponse antiCacheResponse = requestAntiCache(responseExportPanel);

        // All savings transactions are on a single page (CSV file)
        final boolean canFetchMore = false;

        return new CSVTransactionsPage(antiCacheResponse.getBody(String.class), canFetchMore);
    }

    private HttpResponse requestExport() {
        final String underscoreValue = "1544130111973";

        final URL url =
                new URL(
                        String.format(
                                "https://banking.ing.at/online-banking/wicket/wicket/page?1-1.IBehaviorListener.0-umsaetzePanel-suche-exportLink&_=%s",
                                underscoreValue));

        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Accept", "application/xml, text/xml, */*; q=0.01")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?1")
                        .header("Wicket-Ajax", "true")
                        .header("Wicket-Ajax-BaseURL", "wicket/page?1")
                        .header("Wicket-FocusedElementId", "id49")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Connection", "keep-alive")
                        .get(HttpResponse.class);
        return response;
    }

    private HttpResponse requestDateRange(
            final HttpResponse message, final Date fromDate, final Date toDate) {

        final int currentPageNo =
                pageNumberFromAjaxLocation(message.getHeaders().getFirst("Ajax-Location"));

        final URL url =
                new URL(
                        "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                + currentPageNo
                                + "-1.IBehaviorListener.0-form-dateRange-ranges");
        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header("Accept", "application/xml, text/xml, */*; q=0.01")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                        + currentPageNo)
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .header("Wicket-Ajax", "true")
                        .header("Wicket-Ajax-BaseURL", "wicket/page?" + currentPageNo)
                        .header("Wicket-FocusedElementId", "id17e")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Connection", "keep-alive")
                        .body(dateRangesForm(fromDate, toDate).serialize())
                        .post(HttpResponse.class);
        return response;
    }

    private HttpResponse requestAntiCache(final HttpResponse responseContainingAntiCacheUrl) {
        final IngAtAntiCacheParser parser =
                new IngAtAntiCacheParser(responseContainingAntiCacheUrl.getBody(String.class));
        final String urlSegment = parser.getUrl();

        final URL url =
                new URL(
                        "https://banking.ing.at/online-banking/wicket/wicket/"
                                + urlSegment.replaceFirst("\\./", ""));
        final int pageNumber = Preconditions.checkNotNull(parser.getPageNumber());

        final HttpResponse response =
                client.request(url)
                        .header("Host", "banking.ing.at")
                        .header(
                                "Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header(
                                "Referer",
                                "https://banking.ing.at/online-banking/wicket/wicket/page?"
                                        + pageNumber)
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .get(HttpResponse.class);

        return response;
    }

    public Optional<HttpResponse> keepAlive() {
        return ingAtSessionStorage.getCurrentUrl().map(url -> getAccountDetails(new URL(url)));
    }
}
