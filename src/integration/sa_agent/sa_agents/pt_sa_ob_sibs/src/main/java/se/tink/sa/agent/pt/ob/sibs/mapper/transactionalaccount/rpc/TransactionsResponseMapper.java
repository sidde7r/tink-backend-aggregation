package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.TransactionsResponse;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;
import se.tink.sa.services.fetch.trans.TransactionEntity;

@Component
public class TransactionsResponseMapper
        implements Mapper<FetchTransactionsResponse, TransactionsResponse> {

    @Autowired private RequestToResponseCommonMapper requestToResponseCommonMapper;
    @Autowired private BookedEntityMapper bookedEntityMapper;
    @Autowired private TransactionLinksEntityMapper transactionLinksEntityMapper;

    @Override
    public FetchTransactionsResponse map(
            TransactionsResponse source, MappingContext mappingContext) {
        FetchTransactionsResponse.Builder destBuilder = FetchTransactionsResponse.newBuilder();
        RequestCommon rc = mappingContext.get(SibsMappingContextKeys.REQUEST_COMMON);
        ResponseCommon responseCommon = requestToResponseCommonMapper.map(rc, mappingContext);
        destBuilder.setResponseCommon(responseCommon);

        TransactionsEntity entity = source.getTransactions();

        if (entity != null) {
            List<TransactionEntity> transactionEntityList =
                    Optional.ofNullable(entity.getBooked()).orElse(Collections.emptyList()).stream()
                            .map(
                                    bookedEntity ->
                                            bookedEntityMapper.map(bookedEntity, mappingContext))
                            .collect(Collectors.toList());
            destBuilder.addAllTransactionsEntity(transactionEntityList);
            destBuilder.setTransactionLinksEntity(
                    transactionLinksEntityMapper.map(entity.getLinks(), mappingContext));
        }

        return destBuilder.build();
    }
}
