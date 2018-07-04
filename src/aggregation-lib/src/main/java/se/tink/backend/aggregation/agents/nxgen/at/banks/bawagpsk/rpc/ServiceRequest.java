package se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.rpc;

import javax.xml.bind.JAXBException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bawagpsk.entities.ServiceRequestEntity;

public class ServiceRequest {
    private Envelope envelope;

    public ServiceRequest(final String serverSessionId) {
        Context context = new Context();
        context.setChannel(BawagPskConstants.CHANNEL);
        context.setLanguage(BawagPskConstants.LANGUAGE);
        context.setDevID(BawagPskConstants.DEV_ID);
        context.setDeviceIdentifier(BawagPskConstants.DEVICE_IDENTIFIER);

        ServiceRequestEntity request = new ServiceRequestEntity();
        request.setServerSessionId(serverSessionId);
        request.setContext(context);
        request.setActionCall(BawagPskConstants.ACTION_CALL);
        request.setParams("");

        Body body = new Body();
        body.setServiceRequestEntity(request);

        envelope = new Envelope();
        envelope.setHeader("");
        envelope.setBody(body);
    }

    public String getXml() throws JAXBException {
        return BawagPskUtils.envelopeToXml(envelope);
    }
}
