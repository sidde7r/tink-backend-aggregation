package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import org.w3c.dom.Document;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormKeys;

public class JsfUpdateResponse extends HtmlResponse {

    public JsfUpdateResponse(String body) {
        super(body);
    }

    public String getUpdate(String updateId) {
        final String expr = String.format("//update[@id='%s']", updateId);
        return evaluateXPath(expr, String.class);
    }

    public Document getUpdateDocument(String updateId) {
        return parseHTML(getUpdate(updateId));
    }

    public String getViewState() {
        return getUpdate(FormKeys.JSF_VIEWSTATE);
    }
}
