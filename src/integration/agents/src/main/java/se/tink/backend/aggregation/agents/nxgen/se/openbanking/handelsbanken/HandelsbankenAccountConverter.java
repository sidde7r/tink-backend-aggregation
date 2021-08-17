package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class HandelsbankenAccountConverter implements HandelsbankenBaseAccountConverter {

    private final TransactionalAccountTypeMapper transactionalAccountTypeTypeMapper =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Allkonto Ung",
                            "Allkonto",
                            "Checkkonto",
                            "Privatkonto",
                            "shb-anställds konto")
                    .put(TransactionalAccountType.CHECKING, "euro privat")
                    // Not transactional accounts
                    .ignoreKeys(
                            // Credit card
                            "allkortskonto",
                            // Business account
                            "valutakonto utan ränta")
                    .build();

    @Override
    public Optional<TransactionalAccount> toTinkAccount(
            AccountsItemEntity accountEntity, AccountDetailsResponse accountDetailsResponse) {
        return transactionalAccountTypeTypeMapper
                .translate(accountEntity.getAccountType())
                .map(type -> accountEntity.toTinkAccount(type, accountDetailsResponse))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
