package se.tink.libraries.abnamro.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.core.Account;

/**
 * Utility class that compares a list of existing accounts with list of active and inactive accounts from ABN AMRO.
 * Return the mismatch between the inputs.
 */
public class AbnAmroAccountCompareUtils {

    private Set<String> oursAll;
    private Set<String> oursActive;
    private Set<String> oursInactive;
    private Set<String> theirsAll;
    private Set<String> theirsActive;
    private Set<String> theirsInactive;

    public AbnAmroAccountCompareUtils(List<Account> ourAccounts, Iterable<String> theirActiveAccountNumbers,
            Iterable<String> theirInactiveAccountNumbers) {
        Preconditions.checkNotNull(ourAccounts);
        Preconditions.checkNotNull(theirActiveAccountNumbers);
        Preconditions.checkNotNull(theirInactiveAccountNumbers);

        oursActive = ourAccounts.stream().filter(a -> !AbnAmroUtils.isAccountRejected(a)).map(Account::getBankId)
                .collect(Collectors.toSet());
        oursInactive = ourAccounts.stream().filter(AbnAmroUtils::isAccountRejected).map(Account::getBankId).collect(
                Collectors.toSet());
        oursAll = Sets.union(oursActive, oursInactive);

        theirsActive = ImmutableSet.copyOf(theirActiveAccountNumbers);
        theirsInactive = ImmutableSet.copyOf(theirInactiveAccountNumbers);
        theirsAll = Sets.union(theirsActive, theirsInactive);
    }

    public Result compare() {
        Result result = new Result();

        // Accounts at Tink but not at Abn Amro
        result.setMissingAtAbnAmro(Sets.difference(oursAll, theirsAll).immutableCopy());

        // Accounts at Abn Amro but not at Tink
        result.setMissingAtTink(Sets.difference(theirsAll, oursAll).immutableCopy());

        // Accounts that are active at ABN AMRO but inactive at Tink
        result.setActiveAtAbnAmroInactiveAtTink(Sets.intersection(theirsActive, oursInactive).immutableCopy());

        // Accounts that are active at Tink but inactive at Abn Amro
        result.setActiveAtTinkInactiveAtAbnAmro(Sets.intersection(oursActive, theirsInactive).immutableCopy());

        return result;
    }

    public class Result {
        private ImmutableSet<String> missingAtAbnAmro;
        private ImmutableSet<String> missingAtTink;
        private ImmutableSet<String> activeAtAbnAmroInactiveAtTink;
        private ImmutableSet<String> activeAtTinkInactiveAtAbnAmro;

        private void setMissingAtAbnAmro(ImmutableSet<String> missingAtAbnAmro) {
            this.missingAtAbnAmro = missingAtAbnAmro;
        }

        private void setMissingAtTink(ImmutableSet<String> missingAtTink) {
            this.missingAtTink = missingAtTink;
        }

        private void setActiveAtAbnAmroInactiveAtTink(ImmutableSet<String> activeAtAbnAmroInactiveAtTink) {
            this.activeAtAbnAmroInactiveAtTink = activeAtAbnAmroInactiveAtTink;
        }

        private void setActiveAtTinkInactiveAtAbnAmro(ImmutableSet<String> activeAtTinkInactiveAtAbnAmro) {
            this.activeAtTinkInactiveAtAbnAmro = activeAtTinkInactiveAtAbnAmro;
        }

        public ImmutableSet<String> getMissingAtAbnAmro() {
            return missingAtAbnAmro;
        }

        public ImmutableSet<String> getMissingAtTink() {
            return missingAtTink;
        }

        public ImmutableSet<String> getActiveAtAbnAmroInactiveAtTink() {
            return activeAtAbnAmroInactiveAtTink;
        }

        public ImmutableSet<String> getActiveAtTinkInactiveAtAbnAmro() {
            return activeAtTinkInactiveAtAbnAmro;
        }

        public boolean isValid() {
            return CollectionUtils.isEmpty(missingAtAbnAmro)
                    && CollectionUtils.isEmpty(missingAtTink)
                    && CollectionUtils.isEmpty(activeAtAbnAmroInactiveAtTink)
                    && CollectionUtils.isEmpty(activeAtTinkInactiveAtAbnAmro);
        }
    }
}
