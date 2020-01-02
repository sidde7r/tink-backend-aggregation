package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GlobalPositionResponse extends HtmlResponse {
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

    public List<Integer> getAccountIds() {
        ArrayList<Integer> accountIds = new ArrayList<>();
        final NodeList accountNodes = getAccountNodes();
        for (int i = 0; i < accountNodes.getLength(); i += 1) {
            final Node node = accountNodes.item(i);
            final int accountId =
                    Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
            // ID used for further requests is id attribute minus one
            accountIds.add(new Integer(accountId - 1));
        }
        return accountIds;
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

    private List<String> getAccountLinks(NodeList nodes, Pattern pattern) {
        ArrayList<String> links = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i += 1) {
            // extract link from attached javascript
            final Node node = nodes.item(i);
            final String script =
                    evaluateXPath(node, "script/comment()", Node.class).getTextContent();
            final Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                links.add(matcher.group(1));
            }
        }
        return links;
    }
}
