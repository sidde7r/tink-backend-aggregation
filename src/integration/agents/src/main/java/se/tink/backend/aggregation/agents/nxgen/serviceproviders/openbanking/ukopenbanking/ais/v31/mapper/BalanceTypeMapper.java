package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;

public interface BalanceTypeMapper {

    GenericTypeMapper<AccountBalanceType, UkObBalanceType> ACCOUNT_BALANCE_TYPE_MAPPER =
            GenericTypeMapper.<AccountBalanceType, UkObBalanceType>genericBuilder()
                    .put(AccountBalanceType.CLEARED_BALANCE, UkObBalanceType.CLEARED_BALANCE)
                    .put(AccountBalanceType.CLOSING_AVAILABLE, UkObBalanceType.CLOSING_AVAILABLE)
                    .put(AccountBalanceType.CLOSING_BOOKED, UkObBalanceType.CLOSING_BOOKED)
                    .put(AccountBalanceType.CLOSING_CLEARED, UkObBalanceType.CLOSING_CLEARED)
                    .put(AccountBalanceType.EXPECTED, UkObBalanceType.EXPECTED)
                    .put(AccountBalanceType.FORWARD_AVAILABLE, UkObBalanceType.FORWARD_AVAILABLE)
                    .put(AccountBalanceType.INFORMATION, UkObBalanceType.INFORMATION)
                    .put(AccountBalanceType.INTERIM_AVAILABLE, UkObBalanceType.INTERIM_AVAILABLE)
                    .put(AccountBalanceType.INTERIM_BOOKED, UkObBalanceType.INTERIM_BOOKED)
                    .put(AccountBalanceType.INTERIM_CLEARED, UkObBalanceType.INTERIM_CLEARED)
                    .put(AccountBalanceType.OPENING_AVAILABLE, UkObBalanceType.OPENING_AVAILABLE)
                    .put(AccountBalanceType.OPENING_BOOKED, UkObBalanceType.OPENING_BOOKED)
                    .put(AccountBalanceType.OPENING_CLEARED, UkObBalanceType.OPENING_CLEARED)
                    .put(
                            AccountBalanceType.PREVIOUSLY_CLOSED_BOOKED,
                            UkObBalanceType.PREVIOUSLY_CLOSED_BOOKED)
                    .build();

    static AccountBalanceType toTinkAccountBalanceType(UkObBalanceType type) {
        return ACCOUNT_BALANCE_TYPE_MAPPER
                .translate(type)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "[TRANSACTIONAL ACCOUNT BALANCE MAPPER] Unknown balance type: "
                                                + type));
    }
}
