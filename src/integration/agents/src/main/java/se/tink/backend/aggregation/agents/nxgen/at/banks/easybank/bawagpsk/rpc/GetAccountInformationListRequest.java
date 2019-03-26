package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountInformationListRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;

public class GetAccountInformationListRequest {
    private Envelope envelope;

    public GetAccountInformationListRequest(
            final String serverSessionID, final String qid, final List<ProductID> productIDs) {
        GetAccountInformationListRequestEntity requestEntity =
                new GetAccountInformationListRequestEntity();
        requestEntity.setServerSessionID(serverSessionID);
        requestEntity.setQid(qid);
        requestEntity.setProductIdList(productIDs);
        Context context = new Context();
        context.setChannel(BawagPskConstants.CHANNEL);
        context.setLanguage(BawagPskConstants.LANGUAGE);
        context.setDevID(BawagPskConstants.DEV_ID);
        context.setDeviceIdentifier(BawagPskConstants.DEVICE_IDENTIFIER);
        requestEntity.setContext(context);

        Body body = new Body();
        body.setGetAccountInformationListRequestEntity(requestEntity);

        envelope = new Envelope();
        envelope.setBody(body);
    }

    public String getXml() {
        return BawagPskUtils.entityToXml(envelope);
    }
}
