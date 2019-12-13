package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.GoogleDateMapperFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg.TransactionEntityMapper;

public class TransactionEntityMapperFactory {

    private final GoogleDateMapperFactory googleDateMapperFactory;

    private final ExactCurrencyAmountMapperFactory exactCurrencyAmountMapperFactory;

    private TransactionEntityMapperFactory(
            GoogleDateMapperFactory googleDateMapperFactory,
            ExactCurrencyAmountMapperFactory exactCurrencyAmountMapperFactory) {
        this.googleDateMapperFactory = googleDateMapperFactory;
        this.exactCurrencyAmountMapperFactory = exactCurrencyAmountMapperFactory;
    }

    public static TransactionEntityMapperFactory newInstance(
            GoogleDateMapperFactory googleDateMapperFactory,
            ExactCurrencyAmountMapperFactory exactCurrencyAmountMapperFactory) {
        return new TransactionEntityMapperFactory(
                googleDateMapperFactory, exactCurrencyAmountMapperFactory);
    }

    public TransactionEntityMapper fetchTransactionEntityMapper() {
        TransactionEntityMapper transactionEntityMapper = new TransactionEntityMapper();
        transactionEntityMapper.setGoogleDateMapper(
                googleDateMapperFactory.fetchGoogleDateMapper());
        transactionEntityMapper.setExactCurrencyAmountMapper(
                exactCurrencyAmountMapperFactory.exactCurrencyAmountMapper());
        return transactionEntityMapper;
    }
}
