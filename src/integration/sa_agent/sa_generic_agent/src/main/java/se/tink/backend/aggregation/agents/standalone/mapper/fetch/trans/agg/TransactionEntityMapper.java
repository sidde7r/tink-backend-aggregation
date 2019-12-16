package se.tink.backend.aggregation.agents.standalone.mapper.fetch.trans.agg;

import java.util.Date;
import se.tink.backend.aggregation.agents.standalone.mapper.common.GoogleDateMapper;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.ExactCurrencyAmountMapper;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.trans.TransactionEntity;

public class TransactionEntityMapper implements Mapper<Transaction, TransactionEntity> {

    private GoogleDateMapper googleDateMapper;

    private ExactCurrencyAmountMapper exactCurrencyAmountMapper;

    public void setGoogleDateMapper(GoogleDateMapper googleDateMapper) {
        this.googleDateMapper = googleDateMapper;
    }

    public void setExactCurrencyAmountMapper(ExactCurrencyAmountMapper exactCurrencyAmountMapper) {
        this.exactCurrencyAmountMapper = exactCurrencyAmountMapper;
    }

    @Override
    public Transaction map(TransactionEntity source, MappingContext mappingContext) {
        Transaction.Builder destBuilder = Transaction.builder();

        ExactCurrencyAmount exactCurrencyAmount =
                exactCurrencyAmountMapper.map(source.getAmount(), mappingContext);
        destBuilder.setAmount(exactCurrencyAmount);

        Date date = googleDateMapper.map(source.getValueDate(), mappingContext);
        destBuilder.setDate(date);

        destBuilder.setDescription(source.getRemittanceInformationUnstructured());

        return destBuilder.build();
    }
}
