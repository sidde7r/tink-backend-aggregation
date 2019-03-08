package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskUtils;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountStatementItemsRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.StatementSearchCriteria;

public class GetAccountStatementItemsRequest {
    private Envelope envelope;

    public GetAccountStatementItemsRequest(
            final String serverSessionID,
            final String qid,
            final ProductID productID,
            final LocalDateTime fromDateTime,
            final LocalDateTime toDateTime) {
        Context context = new Context();
        context.setChannel(BawagPskConstants.CHANNEL);
        context.setLanguage(BawagPskConstants.LANGUAGE);
        context.setDevID(BawagPskConstants.DEV_ID);
        context.setDeviceIdentifier(BawagPskConstants.DEVICE_IDENTIFIER);

        FinancialInstitute institute = new FinancialInstitute();
        institute.setBankCode(BawagPskConstants.Server.BANK_CODE);
        institute.setBIC(BawagPskConstants.Server.BIC);
        institute.setCode(BawagPskConstants.Server.CODE);
        institute.setShortName(BawagPskConstants.Server.SHORT_NAME);

        StatementSearchCriteria criteria = new StatementSearchCriteria();
        criteria.setMinDatePosted(fromDateTime);
        criteria.setMaxDatePosted(toDateTime);
        criteria.setSortingColumn(BawagPskConstants.BOOKING_DATE);
        criteria.setTransactionType(BawagPskConstants.TRANSACTION_TYPE);

        GetAccountStatementItemsRequestEntity request = new GetAccountStatementItemsRequestEntity();
        request.setServerSessionID(serverSessionID);
        request.setContext(context);
        request.setQid(qid);
        request.setProductID(productID);
        request.setStatementSearchCriteria(criteria);

        Body body = new Body();
        body.setGetAccountStatementItemsRequestEntity(request);

        envelope = new Envelope();
        envelope.setHeader("");
        envelope.setBody(body);
    }

    public String getXml() {
        return BawagPskUtils.entityToXml(envelope);
    }
}
