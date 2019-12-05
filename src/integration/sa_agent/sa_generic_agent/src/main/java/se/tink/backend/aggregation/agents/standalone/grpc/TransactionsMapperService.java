package se.tink.backend.aggregation.agents.standalone.grpc;

import java.util.*;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.FetchAccountsResponseMapperFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.TransactionaAccountMapper;
import se.tink.sa.services.fetch.trans.PayloadMap;
import se.tink.sa.services.fetch.trans.TransactionsMapEntity;

public class TransactionsMapperService {

    public static se.tink.sa.services.fetch.trans.FetchTransactionsRequest
            mapFetchTransactionsRequest() {
        return se.tink.sa.services.fetch.trans.FetchTransactionsRequest.newBuilder().build();
    }

    public static FetchTransactionsResponse mapFetchTransactionsResponse(
            final se.tink.sa.services.fetch.trans.FetchTransactionsResponse
                    fetchTransactionsResponse) {
        return new FetchTransactionsResponse(
                mapTransactions(fetchTransactionsResponse.getTransactionsList()));
    }

    private static Map<Account, List<Transaction>> mapTransactions(
            final List<TransactionsMapEntity> transactionsMapEntityList) {
        TransactionaAccountMapper transactionaAccountMapper =
                FetchAccountsResponseMapperFactory.transactionaAccountMapper();
        return Optional.ofNullable(transactionsMapEntityList).orElse(Collections.emptyList())
                .stream()
                .collect(
                        Collectors.toMap(
                                transactionsMapEntity ->
                                        transactionaAccountMapper.map(
                                                transactionsMapEntity.getKey()),
                                transactionsMapEntity ->
                                        TransactionsMapperService.mapTransactionList(
                                                transactionsMapEntity.getValueList())));
    }

    private static List<Transaction> mapTransactionList(
            final List<se.tink.sa.services.fetch.trans.Transaction> transaction) {
        return Optional.ofNullable(transaction).orElse(Collections.emptyList()).stream()
                .map(TransactionsMapperService::mapTransaction)
                .collect(Collectors.toList());
    }

    private static Transaction mapTransaction(
            final se.tink.sa.services.fetch.trans.Transaction transaction) {
        final Transaction resp = new Transaction();
        resp.setAccountId(transaction.getAccountId());
        resp.setAmount(transaction.getAmount());
        resp.setCredentialsId(transaction.getCredentialsId());
        resp.setDate(mapFromGoogleDate(transaction.getDate()));
        resp.setDescription(transaction.getDescription());
        resp.setId(transaction.getId());
        setInternalPayload(resp, transaction.getInternalPayloadMap());
        resp.setOriginalAmount(transaction.getOriginalAmount());
        setPayload(resp, transaction.getPayloadList());
        resp.setPending(transaction.getPending());
        resp.setTimestamp(transaction.getTimestamp());
        resp.setType(mapTransactionTypes(transaction.getType()));
        resp.setUserId(transaction.getUserId());
        resp.setUpcoming(transaction.getUpcoming());
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

    public static Date mapFromGoogleDate(final com.google.type.Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, date.getDay());
        return calendar.getTime();
    }
}
