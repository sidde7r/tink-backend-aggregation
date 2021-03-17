package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.VisibleForTesting;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class HtmlResponse {
    protected final String body;
    protected final Document document;
    private static final XPathFactory xpathFactory = XPathFactory.newInstance();
    private final DecimalFormat amountFormat;
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?<value>[\\+\\-]?[0-9\\.,]+)(?<currency>€|EUROS|\\$|\\w{3})?");
    private static final DateTimeFormatter TRANSACTION_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern TRANSACTION_DATE_PATTERN =
            Pattern.compile("(?:\\w+) (\\d{2}/\\d{2}/\\d{4})");

    /**
     * Removes occurrences of "--" from HTML comments, since it cannot be parsed. All occurrences of
     * "--" in comments are replaced with "__".
     *
     * @param body HTML which may have "--" in comments.
     * @return body with occurrences of "--" in comments replaced with "__"
     */
    @VisibleForTesting
    protected static String removeDoubleDashesFromComments(String body) {
        final Pattern commentPattern = Pattern.compile("<!--(.*?)-->", Pattern.DOTALL);
        final Matcher matcher = commentPattern.matcher(body);
        final StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            final String comment = body.substring(matcher.start() + 4, matcher.end() - 3);
            final String replacedComment =
                    "<!--" + Matcher.quoteReplacement(comment.replace("--", "__")) + "-->";
            matcher.appendReplacement(buffer, replacedComment);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    protected static Document parseHTML(String body) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setCoalescing(false);
        factory.setXIncludeAware(false);

        final String adaptedBody = removeDoubleDashesFromComments(body);
        try (InputStream inputStream = IOUtils.toInputStream(adaptedBody, StandardCharsets.UTF_8)) {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            return factory.newDocumentBuilder().parse(inputStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            return fallbackParse(adaptedBody);
        }
    }

    public HtmlResponse(String body) {
        this.amountFormat =
                new DecimalFormat(
                        "#,##0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es")));
        this.amountFormat.setParseBigDecimal(true);

        this.body = body;
        this.document = parseHTML(this.body);
    }

    @VisibleForTesting
    protected ExactCurrencyAmount parseAmount(String amountString) {
        final String amountWithoutSpaces = amountString.replaceAll("[\\s\\u00a0]+", "");
        final Matcher matcher = AMOUNT_PATTERN.matcher(amountWithoutSpaces);
        if (!matcher.matches()) {
            throw new IllegalStateException(
                    "Unexpected amount format for '" + amountWithoutSpaces + "'");
        }

        final String amountValue = matcher.group("value");
        final String amountCurrency = matcher.group("currency");

        return ExactCurrencyAmount.of(parseValue(amountValue), parseCurrency(amountCurrency));
    }

    protected String parseCurrency(String currency) {
        if (Strings.isNullOrEmpty(currency)) {
            return BankinterConstants.DEFAULT_CURRENCY;
        } else if (currency.equals("€") || currency.equalsIgnoreCase("EUROS")) {
            return "EUR";
        } else if (currency.equals("$")) {
            return "USD";
        } else {
            return currency;
        }
    }

    protected BigDecimal parseValue(String amountString) {
        try {
            if (amountString.startsWith("+")) {
                return (BigDecimal) amountFormat.parse(amountString.substring(1));
            } else {
                return (BigDecimal) amountFormat.parse(amountString);
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Could not parse amount " + amountString, e);
        }
    }

    protected LocalDate parseTransactionDate(String date) {
        final Matcher dateMatcher = TRANSACTION_DATE_PATTERN.matcher(date);
        if (!dateMatcher.find()) {
            throw new IllegalStateException("Could not parse transaction date: " + date);
        }
        return LocalDate.parse(dateMatcher.group(1), TRANSACTION_DATE_FORMATTER);
    }

    private static QName returnTypeForClass(Class cls) {
        if (cls == String.class) {
            return XPathConstants.STRING;
        } else if (cls == Node.class || cls == Element.class) {
            return XPathConstants.NODE;
        } else if (cls == NodeList.class) {
            return XPathConstants.NODESET;
        } else if (cls == Double.class) {
            return XPathConstants.NUMBER;
        } else if (cls == Boolean.class) {
            return XPathConstants.BOOLEAN;
        } else {
            throw new NotImplementedException("No return type for " + cls.getCanonicalName());
        }
    }

    private static Document fallbackParse(String input) {
        try {
            return W3CDom.convert(Jsoup.parse(input));
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not parse HTML response", e);
        }
    }

    protected static <T> T evaluateXPath(Node node, String xPathExpression, Class<T> returnClass) {
        final XPath xPath = xpathFactory.newXPath();
        final XPathExpression expr;
        try {
            expr = xPath.compile(xPathExpression);
            return (T) expr.evaluate(node, returnTypeForClass(returnClass));
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(
                    "Error evaluating XPath expression: " + xPathExpression, e);
        }
    }

    protected <T> T evaluateXPath(String xPathExpression, Class<T> returnClass) {
        return evaluateXPath(this.document, xPathExpression, returnClass);
    }

    protected @Nullable Element getElementById(String id) {
        // because we're parsing without a DTD, document.getElementById doesn't work
        return (Element) evaluateXPath(String.format("//*[@id = '%s']", id), Node.class);
    }

    public String getViewState(String formId) {
        final String expr =
                String.format("//form[@id='%s']//input[@name='javax.faces.ViewState']", formId);
        return evaluateXPath(expr, Node.class).getAttributes().getNamedItem("value").getNodeValue();
    }

    public String getBody() {
        return body;
    }

    public boolean hasError() {
        final String error = evaluateXPath("//div[contains(@class, 'genericError')]", String.class);
        return StringUtils.isNotBlank(error);
    }
}
