package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.constants.RpcConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.ServiceRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.utils.EntitiesUtils;

public class ServiceRequest {
    private Envelope envelope;

    public ServiceRequest(final String serverSessionId) {
        Context context = new Context();
        context.setChannel(RpcConstants.CHANNEL);
        context.setLanguage(RpcConstants.LANGUAGE);
        context.setDevID(RpcConstants.DEV_ID);
        context.setDeviceIdentifier(RpcConstants.DEVICE_IDENTIFIER);

        ServiceRequestEntity request = new ServiceRequestEntity();
        request.setServerSessionId(serverSessionId);
        request.setContext(context);
        request.setActionCall(RpcConstants.ACTION_CALL);
        request.setParams("");

        Body body = new Body();
        body.setServiceRequestEntity(request);

        envelope = new Envelope();
        envelope.setHeader("");
        envelope.setBody(body);
    }

    public String getXml() {
        return EntitiesUtils.entityToXml(envelope);
    }
}
