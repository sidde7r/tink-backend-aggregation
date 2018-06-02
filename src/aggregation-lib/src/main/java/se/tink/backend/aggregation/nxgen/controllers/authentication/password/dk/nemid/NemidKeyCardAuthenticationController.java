package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;

public class NemidKeyCardAuthenticationController extends NemidAuthenticationController implements
        KeyCardAuthenticator {

    private static final By LOGIN_BUTTON_FORM_1 = By.cssSelector("button.Box-Button-Submit");
    private static final By CARD_NUMBER = By.cssSelector("span.otp__card-number");
    private static final By CARD_INDEX_CELLS = By.cssSelector("div.otp__frame__cell");
    private static final By CARD_INPUT_FIELD = By.cssSelector("input.otp-input");

    public NemidKeyCardAuthenticationController(NemIdAuthenticator authenticator) {
        super(authenticator);
        throw new UnsupportedOperationException("Cannot be used until step 2 can be succesfully completed");
    }

    @Override
    void clickLogin() {
        clickButton(LOGIN_BUTTON_FORM_1);
    }

    @Override
    public KeyCardInitValues init(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            doLoginWith(username, password);
            return new KeyCardInitValues(collectCardId(), collectCardIndex());
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    private String collectCardId() {
        return waitForElement(CARD_NUMBER)
                .map(WebElement::getText)
                .orElse(null);
    }

    private String collectCardIndex() {
        return waitForElement(CARD_INDEX_CELLS)//There are multiple, we only want the first
                .map(WebElement::getText)
                .orElseThrow(() -> new IllegalStateException("Cannot find card index in form."));
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        try {
            waitForElement(CARD_INPUT_FIELD)
                    .orElseThrow(() -> new IllegalStateException("Cannot find input field for card code"))
                    .sendKeys(code);
            clickButton(SUBMIT_BUTTON);

            passTokenToAuthenticator();
        } catch (Exception e) {
            logGeneralError();
            throw e;
        } finally {
            close();
        }
    }

}
