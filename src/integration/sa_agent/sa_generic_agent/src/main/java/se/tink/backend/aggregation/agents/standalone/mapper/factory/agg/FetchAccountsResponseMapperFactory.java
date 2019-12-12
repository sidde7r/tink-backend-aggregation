package se.tink.backend.aggregation.agents.standalone.mapper.factory.agg;

import se.tink.backend.aggregation.agents.standalone.mapper.factory.sa.CommonMappersFactory;
import se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg.*;

public final class FetchAccountsResponseMapperFactory {

    private final CommonMappersFactory commonMappersFactory;

    private FetchAccountsResponseMapperFactory(CommonMappersFactory commonMappersFactory) {
        this.commonMappersFactory = commonMappersFactory;
    }

    public static FetchAccountsResponseMapperFactory newInstance(
            CommonMappersFactory commonMappersFactory) {
        return new FetchAccountsResponseMapperFactory(commonMappersFactory);
    }

    public FetchAccountsResponseMapper fetchAccountsResponseMapper() {
        FetchAccountsResponseMapper mapper = new FetchAccountsResponseMapper();
        mapper.setTransactionaAccountMapper(transactionalAccountMapper());
        return mapper;
    }

    public TransactionAccountMapper transactionalAccountMapper() {
        TransactionAccountMapper mapper = new TransactionAccountMapper();
        mapper.setExactCurrencyAmountMapper(exactCurrencyAmountMapper());
        mapper.setAccountTypesMapper(accountTypesMapper());
        mapper.setBalanceModuleMapper(balanceModuleMapper());
        mapper.setIbanIdModuleMapper(ibanIdModuleMapper());
        return mapper;
    }

    private ExactCurrencyAmountMapper exactCurrencyAmountMapper(){
        return new ExactCurrencyAmountMapper();
    }

    private AccountTypesMapper accountTypesMapper(){
        return new AccountTypesMapper();
    }

    private BalanceModuleMapper balanceModuleMapper(){
        return new BalanceModuleMapper();
    }

    private IbanIdModuleMapper ibanIdModuleMapper(){
        return new IbanIdModuleMapper();
    }


}
