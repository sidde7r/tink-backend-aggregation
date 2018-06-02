package se.tink.backend.utils.guavaimpl.comparators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import se.tink.libraries.account.AccountIdentifier;

public class AccountIdentifierComparator {
    // For now only one type is a "bad" type, the rest we don't prioritize for now but could be done later on more dynamically
    private static final ImmutableList<AccountIdentifier.Type> LOW_PRIORITY_TYPES = ImmutableList.of(
            AccountIdentifier.Type.SE_SHB_INTERNAL);

    // Result is inverted to get prioritization from high to low
    public static final Comparator<? super AccountIdentifier> IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW = new Comparator<AccountIdentifier>() {
        @Override
        public int compare(AccountIdentifier left, AccountIdentifier right) {
            return invert(ComparisonChain.start()
                    .compare(left.isValid(), right.isValid())
                    .compare(!LOW_PRIORITY_TYPES.contains(left.getType()), !LOW_PRIORITY_TYPES.contains(right.getType()))
                    .result());
        }

        private int invert(int i) {
            return -1 * i;
        }
    };
}
