package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc;

import javax.xml.bind.JAXBException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.LogoutRequestEntity;

public class LogoutRequest {
    private Envelope envelope;

    public LogoutRequest(final String serverSessionID) {
        Context context = new Context();
        context.setChannel(BawagPskConstants.CHANNEL);
        context.setLanguage(BawagPskConstants.LANGUAGE);
        context.setDevID(BawagPskConstants.DEV_ID);
        context.setDeviceIdentifier(BawagPskConstants.DEVICE_IDENTIFIER);

        LogoutRequestEntity request = new LogoutRequestEntity();
        request.setServerSessionID(serverSessionID);
        request.setContext(context);

        Body body = new Body();
        body.setLogoutRequestEntity(request);

        envelope = new Envelope();
        envelope.setBody(body);
        envelope.setHeader("");
    }

    public String getXml() throws JAXBException {
        return BawagPskUtils.envelopeToXml(envelope);
    }
}
