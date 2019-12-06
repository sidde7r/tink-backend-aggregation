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
        mapper.setTransactionaAccountMapper(transactionaAccountMapper());
        return mapper;
    }

    public TransactionaAccountMapper transactionaAccountMapper() {
        TransactionaAccountMapper mapper = new TransactionaAccountMapper();
        mapper.setAccountExclusionMapper(accountExclusionMapper());
        mapper.setExactCurrencyAmountMapper(exactCurrencyAmountMapper());
        mapper.setGoogleDateMapper(commonMappersFactory.googleDateMapper());
        mapper.setAccountTypesMapper(accountTypesMapper());
        mapper.setAccountIdentifierMapper(accountIdentifierMapper());
        mapper.setTransferDestinationMapper(transferDestinationMapper());
        mapper.setAccountDetailsMapper(accountDetailsMapper());
        mapper.setAccountFlagMapper(accountFlagMapper());
        return mapper;
    }

    private AccountExclusionMapper accountExclusionMapper() {
        return new AccountExclusionMapper();
    }

    private AccountFlagMapper accountFlagMapper() {
        return new AccountFlagMapper();
    }

    private AccountIdentifierMapper accountIdentifierMapper() {
        return new AccountIdentifierMapper();
    }

    private AccountTypesMapper accountTypesMapper() {
        return new AccountTypesMapper();
    }

    private ExactCurrencyAmountMapper exactCurrencyAmountMapper() {
        return new ExactCurrencyAmountMapper();
    }

    private TransferDestinationMapper transferDestinationMapper() {
        return new TransferDestinationMapper();
    }

    private AccountDetailsMapper accountDetailsMapper() {
        AccountDetailsMapper accountDetailsMapper = new AccountDetailsMapper();
        accountDetailsMapper.setGoogleDateMapper(commonMappersFactory.googleDateMapper());
        return accountDetailsMapper;
    }
}
