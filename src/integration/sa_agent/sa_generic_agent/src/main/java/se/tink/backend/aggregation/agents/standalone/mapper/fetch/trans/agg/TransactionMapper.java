package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.standalone.mapper.common.GoogleDateMapper;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.trans.PayloadMap;

public class TransactionMapper
        implements Mapper<Transaction, se.tink.sa.services.fetch.trans.Transaction> {

    private GoogleDateMapper googleDateMapper;

    public void setGoogleDateMapper(GoogleDateMapper googleDateMapper) {
        this.googleDateMapper = googleDateMapper;
    }

    @Override
    public Transaction map(
            se.tink.sa.services.fetch.trans.Transaction source, MappingContext mappingContext) {
        final Transaction resp = new Transaction();
        resp.setAccountId(source.getAccountId());
        resp.setAmount(source.getAmount());
        resp.setCredentialsId(source.getCredentialsId());
        resp.setDate(googleDateMapper.map(source.getDate(), mappingContext));
        resp.setDescription(source.getDescription());
        resp.setId(source.getId());
        setInternalPayload(resp, source.getInternalPayloadMap());
        resp.setOriginalAmount(source.getOriginalAmount());
        setPayload(resp, source.getPayloadList());
        resp.setPending(source.getPending());
        resp.setTimestamp(source.getTimestamp());
        resp.setType(mapTransactionTypes(source.getType()));
        resp.setUserId(source.getUserId());
        resp.setUpcoming(source.getUpcoming());
        return resp;
    }

    private static void setInternalPayload(
            final Transaction resp, final Map<String, String> internalPayload) {
        Optional.ofNullable(internalPayload).orElse(Collections.emptyMap()).entrySet().stream()
                .forEach(entry -> resp.setInternalPayload(entry.getKey(), entry.getValue()));
    }

    private static void setPayload(final Transaction resp, final List<PayloadMap> payloadMap) {
        Optional.ofNullable(payloadMap).orElse(Collections.emptyList()).stream()
                .forEach(
                        payload ->
                                resp.setPayload(
                                        mapTransactionPayloadTypes(payload.getKey()),
                                        payload.getValue()));
    }

    private static TransactionPayloadTypes mapTransactionPayloadTypes(
            final se.tink.sa.services.fetch.trans.TransactionPayloadTypes transactionPayloadTypes) {
        return TransactionPayloadTypes.values()[transactionPayloadTypes.getNumber()];
    }

    private static TransactionTypes mapTransactionTypes(
            final se.tink.sa.services.fetch.trans.TransactionTypes type) {
        return TransactionTypes.values()[type.getNumber()];
    }
}
