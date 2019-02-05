package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ServiceRequestEntity;

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

    public String getXml() {
        return BawagPskUtils.entityToXml(envelope);
    }
}
