package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.mapper;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.ProductFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class BarclaysCorporateAccountTypeMapper implements AccountTypeMapper {

    private static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER_BASE_ON_PRODUCT =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "BusinessCurrentAccount")
                    .put(AccountTypes.CREDIT_CARD, "CommercialCreditCard")
                    .build();

    private final ProductFetcher productFetcher;

    public BarclaysCorporateAccountTypeMapper(ProductFetcher productFetcher) {
        this.productFetcher = productFetcher;
    }

    @Override
    public AccountTypes getAccountType(AccountEntity accountEntity) {
        return ACCOUNT_TYPE_MAPPER
                .translate(accountEntity.getRawAccountSubType())
                .orElseGet(
                        () ->
                                productFetcher
                                        .fetchProduct(accountEntity.getAccountId())
                                        .flatMap(
                                                res ->
                                                        ACCOUNT_TYPE_MAPPER_BASE_ON_PRODUCT
                                                                .translate(
                                                                        res.getAccountProductType(
                                                                                accountEntity
                                                                                        .getAccountId())))
                                        .orElseThrow(
                                                () ->
                                                        new IllegalArgumentException(
                                                                "Unexpected product type has appeared")));
    }

    @Override
    public AccountOwnershipType getAccountOwnershipType(AccountEntity account) {
        return AccountOwnershipType.BUSINESS;
    }

    @Override
    public boolean supportsAccountOwnershipType(AccountEntity accountEntity) {
        return true;
    }
}
