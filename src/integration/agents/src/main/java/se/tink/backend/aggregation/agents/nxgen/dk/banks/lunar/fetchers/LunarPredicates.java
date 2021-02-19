package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers;

import java.util.function.Predicate;
import org.apache.commons.lang3.BooleanUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.BaseResponseEntity;

public class LunarPredicates {

    private static final String LUNAR_BANK_NAME = "Lunar";

    public static Predicate<AccountEntity> lunarAccount() {
        return account -> LUNAR_BANK_NAME.equalsIgnoreCase(account.getBankName());
    }

    public static Predicate<BaseResponseEntity> notDeleted() {
        return account -> BooleanUtils.isNotTrue(account.getDeleted());
    }
}
