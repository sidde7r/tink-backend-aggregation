package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class AccountTypeMapperBuilder {

    public static TransactionalAccountTypeMapper build() {
        return TransactionalAccountTypeMapper.builder()
                .put(TransactionalAccountType.CHECKING, AccountFlag.PSD2_PAYMENT_ACCOUNT, "CACC")
                .ignoreKeys("CARD")
                .build();
    }
}
