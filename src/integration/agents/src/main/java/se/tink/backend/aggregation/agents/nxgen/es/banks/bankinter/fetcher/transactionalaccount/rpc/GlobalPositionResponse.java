package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.HtmlResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class GlobalPositionResponse extends HtmlResponse {

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
}
