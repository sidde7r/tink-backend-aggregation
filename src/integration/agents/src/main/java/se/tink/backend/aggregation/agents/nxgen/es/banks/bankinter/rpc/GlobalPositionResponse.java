package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class GlobalPositionResponse extends HtmlResponse {
    private static final Pattern INVESTMENT_LINK_PATTERN =
            Pattern.compile("'(/fondos/secure/fondo_inversion\\.xhtml?[^']+)'");

    public GlobalPositionResponse(HttpResponse response) {
        super(response);
    }

    private NodeList getAccountNodes() {
        return evaluateXPath("//li[contains(@class,'goAccounts')]", NodeList.class);
    }

    public long getNumberOfAccounts() {
        return getAccountNodes().getLength();
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

    public long getNumberOfInvestments() {
        return getInvestmentNodes().getLength();
    }

    public List<String> getInvestmentLinks() {
        ArrayList<String> links = new ArrayList<>();
        final NodeList nodes = getInvestmentNodes();
        for (int i = 0; i < nodes.getLength(); i += 1) {
            // extract link from attached javascript
            final Node node = nodes.item(i);
            final String script =
                    evaluateXPath(node, "script/comment()", Node.class).getTextContent();
            final Matcher matcher = INVESTMENT_LINK_PATTERN.matcher(script);
            if (matcher.find()) {
                links.add(matcher.group(1));
            }
        }
        return links;
    }
}
