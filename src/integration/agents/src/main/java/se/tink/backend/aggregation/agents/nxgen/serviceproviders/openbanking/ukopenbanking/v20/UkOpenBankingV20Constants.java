package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class UkOpenBankingV20Constants extends UkOpenBankingConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CurrentAccount")
                    .put(AccountTypes.CREDIT_CARD, "CreditCard")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .put(AccountTypes.LOAN, "Loan")
                    .put(AccountTypes.MORTGAGE, "Mortgage")
                    .ignoreKeys("ChargeCard", "EMoney", "PrePaidCard")
                    .build();

    public enum AccountIdentifier {
        IBAN,
        SORT_CODE_ACCOUNT_NUMBER,
        PAN;

        private static final ImmutableList<AccountIdentifier> PREFERRED_ACCOUNT_IDENTIFIERS =
                ImmutableList.<AccountIdentifier>builder()
                        .add(IBAN)
                        .add(SORT_CODE_ACCOUNT_NUMBER)
                        .add(PAN)
                        .build();

        public static Optional<AccountIdentifier> getPreferredIdentifierType(
                Set<AccountIdentifier> typeSet) {
            for (AccountIdentifier id : PREFERRED_ACCOUNT_IDENTIFIERS) {
                if (typeSet.contains(id)) {
                    return Optional.of(id);
                }
            }

            return Optional.empty();
        }

        @JsonCreator
        public static AccountIdentifier fromString(String key) {
            return (key != null)
                    ? AccountIdentifier.valueOf(
                            CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key))
                    : null;
        }
    }

    public static class Links {
        public static final String NEXT = "Next";
    }

    public static class ModelAttributes {
        public static final String SCHEME_NAME = "SchemeName";
        public static final String CREDIT_LINE_TYPE = "Type";
    }
}
