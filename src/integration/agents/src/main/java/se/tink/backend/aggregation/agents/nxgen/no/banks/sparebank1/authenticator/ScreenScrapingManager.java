package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.BankIdErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitBankIdParams;

public class ScreenScrapingManager {

    public static InitBankIdParams getBankIdInitParams(String html) {
        Document initBankIdDoc = Jsoup.parse(html);
        Element form;
        Element viewState;
        try {
            form = initBankIdDoc.getElementById("panel-bankID-mobile").select("form").first();
            viewState = form.getElementById("j_id1:javax.faces.ViewState:1");
        } catch (NullPointerException ex) {
            throw LoginError.DEFAULT_MESSAGE.exception("Missing bank id init params: " + html);
        }
        return new InitBankIdParams(form.id(), viewState.val());
    }

    public static String getPollingElement(String html) {
        Document doc = Jsoup.parse(html);
        Element pollingElement = doc.getElementById("bim-polling");

        if (pollingElement == null) {
            handleBankIdError(doc);
        }
        return pollingElement.select("h1").first().text();
    }

    private static void handleBankIdError(Document doc) throws LoginException, BankIdException {
        try {
            handleKnownBankIdErrorCodes(
                    doc.getElementsByClass("bid-error-wrapper")
                            .first()
                            .select("input")
                            .first()
                            .val()
                            .toLowerCase(),
                    doc);
        } catch (NullPointerException ex) {
            checkForErrorMessage(doc);
        }
    }

    private static void handleKnownBankIdErrorCodes(String bankIdErrorCode, Document doc)
            throws LoginException, BankIdException {
        switch (bankIdErrorCode) {
            case BankIdErrorCodes.C161:
                throw LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.exception();
            case BankIdErrorCodes.C167:
                throw BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE.exception();
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        "Bank Id error code: " + bankIdErrorCode + "\n" + doc.outerHtml());
        }
    }

    private static void checkForErrorMessage(Document doc) {
        try {
            String errorMessage =
                    doc.getElementsByClass("infobox-warning")
                            .first()
                            .getElementsByClass("infobox-content")
                            .first()
                            .select("li")
                            .first()
                            .text();
            throw LoginError.DEFAULT_MESSAGE.exception(errorMessage);
        } catch (NullPointerException ex) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Unknown reason of missing polling element: \n" + doc.outerHtml());
        }
    }
}
