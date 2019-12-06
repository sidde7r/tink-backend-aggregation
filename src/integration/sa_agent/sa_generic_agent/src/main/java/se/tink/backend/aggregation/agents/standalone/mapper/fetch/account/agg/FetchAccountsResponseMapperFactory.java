package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

public final class FetchAccountsResponseMapperFactory {

    public static FetchAccountsResponseMapper buildFetchAccountsResponseMapper() {
        FetchAccountsResponseMapper mapper = new FetchAccountsResponseMapper();
        mapper.setTransactionaAccountMapper(transactionaAccountMapper());
        return mapper;
    }

    public static TransactionaAccountMapper transactionaAccountMapper() {
        TransactionaAccountMapper mapper = new TransactionaAccountMapper();
        mapper.setAccountExclusionMapper(accountExclusionMapper());
        mapper.setExactCurrencyAmountMapper(exactCurrencyAmountMapper());
        mapper.setGoogleDateMapper(googleDateMapper());
        mapper.setAccountTypesMapper(accountTypesMapper());
        mapper.setAccountIdentifierMapper(accountIdentifierMapper());
        mapper.setTransferDestinationMapper(transferDestinationMapper());
        mapper.setAccountDetailsMapper(accountDetailsMapper());
        mapper.setAccountFlagMapper(accountFlagMapper());
        return mapper;
    }

    private static AccountExclusionMapper accountExclusionMapper() {
        return new AccountExclusionMapper();
    }

    private static AccountFlagMapper accountFlagMapper() {
        return new AccountFlagMapper();
    }

    private static AccountIdentifierMapper accountIdentifierMapper() {
        return new AccountIdentifierMapper();
    }

    private static AccountTypesMapper accountTypesMapper() {
        return new AccountTypesMapper();
    }

    private static ExactCurrencyAmountMapper exactCurrencyAmountMapper() {
        return new ExactCurrencyAmountMapper();
    }

    private static FetchAccountsResponseMapper fetchAccountsResponseMapper() {
        FetchAccountsResponseMapper mapper = new FetchAccountsResponseMapper();
        mapper.setTransactionaAccountMapper(transactionaAccountMapper());
        return mapper;
    }

    private static TransferDestinationMapper transferDestinationMapper() {
        return new TransferDestinationMapper();
    }

    private static AccountDetailsMapper accountDetailsMapper() {
        AccountDetailsMapper accountDetailsMapper = new AccountDetailsMapper();
        accountDetailsMapper.setGoogleDateMapper(googleDateMapper());
        return accountDetailsMapper;
    }

    private static GoogleDateMapper googleDateMapper() {
        return new GoogleDateMapper();
    }
}
