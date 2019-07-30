package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.constants.RpcConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.LogoutRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.utils.EntitiesUtils;

public class LogoutRequest {
    private Envelope envelope;

    public LogoutRequest(final String serverSessionID) {
        Context context = new Context();
        context.setChannel(RpcConstants.CHANNEL);
        context.setLanguage(RpcConstants.LANGUAGE);
        context.setDevID(RpcConstants.DEV_ID);
        context.setDeviceIdentifier(RpcConstants.DEVICE_IDENTIFIER);

        LogoutRequestEntity request = new LogoutRequestEntity();
        request.setServerSessionID(serverSessionID);
        request.setContext(context);

        Body body = new Body();
        body.setLogoutRequestEntity(request);

        envelope = new Envelope();
        envelope.setBody(body);
        envelope.setHeader("");
    }

    public String getXml() {
        return EntitiesUtils.entityToXml(envelope);
    }
}
