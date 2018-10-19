package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.rpc.AccountTypes;

public abstract class UkOpenBankingV20Constants extends UkOpenBankingConstants {

    public enum AccountIdentifier {
        IBAN,
        SORT_CODE_ACCOUNT_NUMBER,
        PAN;

        private static final ImmutableList<AccountIdentifier> PREFERRED_ACCOUNT_IDENTIFIERS = ImmutableList.<AccountIdentifier>builder()
                .add(IBAN)
                .add(SORT_CODE_ACCOUNT_NUMBER)
                .add(PAN)
                .build();

        public static Optional<AccountIdentifier> getPreferredIdentifierType(Set<AccountIdentifier> typeSet) {
            for (AccountIdentifier id : PREFERRED_ACCOUNT_IDENTIFIERS) {
                if (typeSet.contains(id)) {
                    return Optional.of(id);
                }
            }

            return Optional.empty();
        }

        @JsonCreator
        public static AccountIdentifier fromString(String key) {
            return (key != null) ?
                    AccountIdentifier.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)) : null;
        }
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

    public static class Links {
        public static final String NEXT = "Next";
    }

    public static class ModelAttributes {
        public static final String SCHEME_NAME = "SchemeName";
    }
}
