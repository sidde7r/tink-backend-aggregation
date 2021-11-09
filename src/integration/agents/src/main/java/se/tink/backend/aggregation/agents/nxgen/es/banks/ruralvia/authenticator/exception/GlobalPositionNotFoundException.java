package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator.exception;

import se.tink.integration.webdriver.exceptions.HtmlElementNotFoundException;

public class GlobalPositionNotFoundException extends HtmlElementNotFoundException {

    public GlobalPositionNotFoundException(String s) {
        super(s);
    }
}
