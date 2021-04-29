package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GlobalPositionResponse extends HtmlResponse {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalPositionResponse.class);
    private static final Pattern ACCOUNT_LINK_PATTERN =
            Pattern.compile("'(/extracto/secure/movimientos_cuenta\\.xhtml\\?[^']+)'");
    private static final Pattern INVESTMENT_LINK_PATTERN =
            Pattern.compile("'(/fondos/secure/fondo_inversion\\.xhtml\\?[^']+)'");
    private static final Pattern CREDIT_CARD_LINK_PATTERN =
            Pattern.compile("'(/tarjetas/secure/tarjetas_ficha\\.xhtml\\?[^']+)'");
    private static final Pattern LOAN_LINK_PATTERN =
            Pattern.compile("'(/prestamos/secure/prestamos\\.xhtml\\?[^']+)'");

    public GlobalPositionResponse(String body) {
        super(body);
    }

    private NodeList getAccountNodes() {
        return evaluateXPath("//li[contains(@class,'goAccounts')]", NodeList.class);
    }

    public List<String> getAccountLinks() {
        return getAccountLinks(getAccountScripts(), ACCOUNT_LINK_PATTERN);
    }

    private List<String> getAccountScripts() {
        return jsoupDocument.getElementsByClass("goAccounts").stream()
                .flatMap(element -> element.children().stream())
                .filter(element -> element.tagName().equals("script"))
                .map(Element::toString)
                .collect(Collectors.toList());
    }

    private NodeList getInvestmentNodes() {
        return evaluateXPath(
                "//li[@data-element='AhorroInversion>BoxFondoInversion']", NodeList.class);
    }

    public List<String> getInvestmentLinks() {
        return getAccountLinks(getInvestmentNodes(), INVESTMENT_LINK_PATTERN);
    }

    private NodeList getCreditCardNodes() {
        return evaluateXPath("//li[@data-element='Tarjetas>BoxTarjeta']", NodeList.class);
    }

    public List<String> getCreditCardLinks() {
        return getAccountLinks(getCreditCardNodes(), CREDIT_CARD_LINK_PATTERN);
    }

    private NodeList getLoanNodes() {
        return evaluateXPath(
                "//li[@data-element='PrestamosCreditos>BoxPrestamosCreditos']", NodeList.class);
    }

    public List<String> getLoanLinks() {
        return getAccountLinks(getLoanNodes(), LOAN_LINK_PATTERN);
    }

    private List<String> getAccountLinks(List<String> scripts, Pattern pattern) {
        ArrayList<String> links = new ArrayList<>();
        for (String script : scripts) {
            final Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                links.add(matcher.group(1));
            } else {
                LOG.warn(String.format("Could not find link for account in script: %s", script));
            }
        }
        return links;
    }

    private List<String> getAccountLinks(NodeList nodes, Pattern pattern) {
        ArrayList<String> links = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i += 1) {
            // extract link from attached javascript
            final Node node = nodes.item(i);
            final Node scriptNode = evaluateXPath(node, "script/comment()", Node.class);
            if (Objects.isNull(scriptNode)) {
                LOG.warn(
                        String.format(
                                "Could not find script for account %d: %s",
                                i, node.getTextContent().trim()));
                continue;
            }
            final String script = scriptNode.getTextContent();
            final Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                links.add(matcher.group(1));
            } else {
                LOG.warn(
                        String.format(
                                "Could not find link for account %d in script: %s",
                                i, node.getTextContent().trim()));
            }
        }
        return links;
    }
}
