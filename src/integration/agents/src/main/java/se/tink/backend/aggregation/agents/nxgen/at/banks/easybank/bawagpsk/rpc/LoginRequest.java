package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Authentication;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.DisposerContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.LoginRequestEntity;

public class LoginRequest {
    private Envelope envelope;

    public LoginRequest(String username, String password, String shortName) {

        Body body = new Body();
        body.setLoginRequestEntity(createRequest(username, password, shortName));
        envelope = new Envelope();
        envelope.setBody(body);
        envelope.setHeader("");
    }

    private LoginRequestEntity createRequest(
            final String username, final String password, final String shortName) {
        FinancialInstitute fininst = new FinancialInstitute();
        fininst.setBankCode(BawagPskConstants.Client.BANK_CODE);
        fininst.setShortName(shortName);

        DisposerContext dcontext = new DisposerContext();

        // DisposerNumber == Username + zero padding
        dcontext.setDisposerNumber(
                StringUtils.leftPad(username, BawagPskConstants.DISPOSER_NUMBER_LENGTH, '0'));
        dcontext.setFinancialInstitute(fininst);

        Context context = new Context();
        context.setChannel(BawagPskConstants.CHANNEL);
        context.setLanguage(BawagPskConstants.LANGUAGE);
        context.setDevID(BawagPskConstants.DEV_ID);
        context.setDeviceIdentifier(BawagPskConstants.DEVICE_IDENTIFIER);

        Authentication authentication = new Authentication();
        authentication.setPin(password);

        LoginRequestEntity loginRequestEntity = new LoginRequestEntity();
        loginRequestEntity.setDisposerContext(dcontext);
        loginRequestEntity.setContext(context);
        loginRequestEntity.setAuthentication(authentication);
        loginRequestEntity.setIsIncludeLoginImage(BawagPskConstants.IS_INCLUDE_LOGIN_IMAGE);

        return loginRequestEntity;
    }

    public String getXml() {
        return BawagPskUtils.entityToXml(envelope);
    }
}
