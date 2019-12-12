package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class TransactionAccountMapper
        implements Mapper<
                TransactionalAccount, se.tink.sa.services.fetch.account.TransactionalAccount> {

    private ExactCurrencyAmountMapper exactCurrencyAmountMapper;

    private AccountTypesMapper accountTypesMapper;

    private BalanceModuleMapper balanceModuleMapper;

    private IbanIdModuleMapper ibanIdModuleMapper;

    public void setExactCurrencyAmountMapper(ExactCurrencyAmountMapper exactCurrencyAmountMapper) {
        this.exactCurrencyAmountMapper = exactCurrencyAmountMapper;
    }

    public void setAccountTypesMapper(AccountTypesMapper accountTypesMapper) {
        this.accountTypesMapper = accountTypesMapper;
    }

    public void setBalanceModuleMapper(BalanceModuleMapper balanceModuleMapper) {
        this.balanceModuleMapper = balanceModuleMapper;
    }

    public void setIbanIdModuleMapper(IbanIdModuleMapper ibanIdModuleMapper) {
        this.ibanIdModuleMapper = ibanIdModuleMapper;
    }

    @Override
    public TransactionalAccount map(
            se.tink.sa.services.fetch.account.TransactionalAccount source,
            MappingContext mappingContext) {
        Optional<TransactionalAccount> dest =
                TransactionalAccount.nxBuilder()
                        .withType(accountTypesMapper.map(source.getType(), mappingContext))
                        .withPaymentAccountFlag()
                        .withBalance(
                                balanceModuleMapper.map(source.getBalanceModule(), mappingContext))
                        .withId(ibanIdModuleMapper.map(source.getId(), mappingContext))
                        .setApiIdentifier(source.getApiId())
                        .build();

        return dest.get();
    }
}
