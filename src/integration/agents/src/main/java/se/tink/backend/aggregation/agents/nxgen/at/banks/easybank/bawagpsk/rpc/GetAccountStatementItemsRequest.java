package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.constants.RpcConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Context;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.GetAccountStatementItemsRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.StatementSearchCriteria;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities.utils.EntitiesUtils;

public class GetAccountStatementItemsRequest {
    private Envelope envelope;

    public GetAccountStatementItemsRequest(
            final String serverSessionID,
            final String qid,
            final ProductID productID,
            final LocalDateTime fromDateTime,
            final LocalDateTime toDateTime) {
        Context context = new Context();
        context.setChannel(RpcConstants.CHANNEL);
        context.setLanguage(RpcConstants.LANGUAGE);
        context.setDevID(RpcConstants.DEV_ID);
        context.setDeviceIdentifier(RpcConstants.DEVICE_IDENTIFIER);

        FinancialInstitute institute = new FinancialInstitute();
        institute.setBankCode(RpcConstants.Server.BANK_CODE);
        institute.setBIC(RpcConstants.Server.BIC);
        institute.setCode(RpcConstants.Server.CODE);
        institute.setShortName(RpcConstants.Server.SHORT_NAME);

        StatementSearchCriteria criteria = new StatementSearchCriteria();
        criteria.setMinDatePosted(fromDateTime);
        criteria.setMaxDatePosted(toDateTime);
        criteria.setSortingColumn(RpcConstants.BOOKING_DATE);
        criteria.setTransactionType(RpcConstants.TRANSACTION_TYPE);

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
        return EntitiesUtils.entityToXml(envelope);
    }
}
