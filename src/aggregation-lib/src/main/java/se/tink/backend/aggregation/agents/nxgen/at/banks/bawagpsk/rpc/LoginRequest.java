package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc;

import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Authentication;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.DisposerContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.LoginRequestEntity;

public class LoginRequest {
    private Envelope envelope;

    public LoginRequest(String username, String password) {

        Body body = new Body();
        body.setLoginRequestEntity(createRequest(username, password));
        envelope = new Envelope();
        envelope.setBody(body);
        envelope.setHeader("");
    }

    private LoginRequestEntity createRequest(final String username, final String password) {
        FinancialInstitute fininst = new FinancialInstitute();
        fininst.setBankCode(BawagPskConstants.CLIENT.BANK_CODE);
        fininst.setShortName(BawagPskConstants.CLIENT.SHORT_NAME);

        DisposerContext dcontext = new DisposerContext();

        // DisposerNumber == Username + zero padding
        dcontext.setDisposerNumber(StringUtils.leftPad(username, BawagPskConstants.DISPOSER_NUMBER_LENGTH, '0'));
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

    public String getXml() throws JAXBException {
        return BawagPskUtils.envelopeToXml(envelope);
    }
}
