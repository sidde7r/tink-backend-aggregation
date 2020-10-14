package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class FeatureTogglesResponseEntity {

    private StateEntity agreements;
    private StateEntity bancontact;
    private StateEntity bankMobility;
    private StateEntity biometrics;
    private StateEntity changeMPin;
    private StateEntity feedback;
    private StateEntity inbox;
    private StateEntity moneyTransfer;
    private StateEntity ocrScannerInMoneyTransfer;
    private StateEntity orders;
    private StateEntity payconiq;
    private StateEntity paymentTemplatesInMoneyTransfer;
    private StateEntity paymentTemplatesManagement;
    private StateEntity productShop;
    private StateEntity psd2GrantOverview;
    private StateEntity sentry;
    private StateEntity thirdPartyGranting;
    private StateEntity transactions;
    private StateEntity trusteer;
    private StateEntity zoomit;
    private StateEntity rootDetection;
    private StateEntity errorLogging;
    private StateEntity personalSettings;
    private StateEntity paymentInitiationService;
    private StateEntity chat;
    private StateEntity experimentAPI;
    private StateEntity authenticatedCall;
    private StateEntity searchServices;
    private StateEntity pushNotification;
}
