package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class UkOpenBankingV30Constants extends UkOpenBankingConstants {

    public static class Links {
        public static final String NEXT = "Next";
    }

    public static class AccountTypeTranslator {
        private static final Logger logger = LoggerFactory.getLogger(AccountTypeTranslator.class);

        private static final ImmutableSet<String> IGNORE_SET = ImmutableSet.<String>builder()
                .add("ChargeCard")
                .add("EMoney")
                .add("PrePaidCard")
                .build();

        private static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPE_MAP = ImmutableMap.<String, AccountTypes>builder()
                .put("CurrentAccount", AccountTypes.CHECKING)
                .put("CreditCard", AccountTypes.CREDIT_CARD)
                .put("Savings", AccountTypes.SAVINGS)
                .put("Loan", AccountTypes.LOAN)
                .put("Mortgage", AccountTypes.MORTGAGE)
                .build();

        public static Optional<AccountTypes> translate(String type) {

            if (IGNORE_SET.contains(type)) {
                return Optional.empty();
            }

            Optional<AccountTypes> accountType = Optional.ofNullable(ACCOUNT_TYPE_MAP.getOrDefault(type, null));
            if (!accountType.isPresent()) {
                logger.info("Unknown account type: %s", type);
            }

            return accountType;
        }
    }
}
