package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.constants.RpcConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Authentication;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.DisposerContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.LoginRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.utils.EntitiesUtils;

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
        fininst.setBankCode(RpcConstants.Client.BANK_CODE);
        fininst.setShortName(shortName);

        DisposerContext dcontext = new DisposerContext();

        // DisposerNumber == Username + zero padding
        dcontext.setDisposerNumber(
                StringUtils.leftPad(username, RpcConstants.DISPOSER_NUMBER_LENGTH, '0'));
        dcontext.setFinancialInstitute(fininst);

        Context context = new Context();
        context.setChannel(RpcConstants.CHANNEL);
        context.setLanguage(RpcConstants.LANGUAGE);
        context.setDevID(RpcConstants.DEV_ID);
        context.setDeviceIdentifier(RpcConstants.DEVICE_IDENTIFIER);

        Authentication authentication = new Authentication();
        authentication.setPin(password);

        LoginRequestEntity loginRequestEntity = new LoginRequestEntity();
        loginRequestEntity.setDisposerContext(dcontext);
        loginRequestEntity.setContext(context);
        loginRequestEntity.setAuthentication(authentication);
        loginRequestEntity.setIsIncludeLoginImage(RpcConstants.IS_INCLUDE_LOGIN_IMAGE);

        return loginRequestEntity;
    }

    public String getXml() {
        return EntitiesUtils.entityToXml(envelope);
    }
}
