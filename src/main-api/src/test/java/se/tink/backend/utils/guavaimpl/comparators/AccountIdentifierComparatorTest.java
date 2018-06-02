package se.tink.backend.utils.guavaimpl.comparators;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountIdentifierComparatorTest {
    @Test
    public void prioritizesValidIdentifiers() {
        int whenBothNotValid = AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW
                .compare(new NonValidIdentifier(null), new NonValidIdentifier(null));
        int whenLeftNotValid = AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW
                .compare(new NonValidIdentifier(null), new SwedishIdentifier("33008607015537"));
        int whenRightNotValid = AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW
                .compare(new SwedishIdentifier("33008607015537"), new NonValidIdentifier(null));

        assertThat(whenBothNotValid).isEqualTo(0);
        assertThat(whenLeftNotValid).isEqualTo(1);
        assertThat(whenRightNotValid).isEqualTo(-1);
    }

    @Test
    public void downPrioritizesSHBInternalIdentifiers() {
        SwedishIdentifier seIdentifier = new SwedishIdentifier("6769952279428");
        SwedishSHBInternalIdentifier seSHBInternalIdentifier = new SwedishSHBInternalIdentifier("952279428");

        int whenBothSHBInternal = AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW
                .compare(seSHBInternalIdentifier, seSHBInternalIdentifier);
        int whenLeftSHBInternal = AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW
                .compare(seSHBInternalIdentifier, seIdentifier);
        int whenRightSHBInternal = AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW
                .compare(seIdentifier, seSHBInternalIdentifier);

        assertThat(whenBothSHBInternal).isEqualTo(0);
        assertThat(whenLeftSHBInternal).isEqualTo(1);
        assertThat(whenRightSHBInternal).isEqualTo(-1);
    }

    @Test
    public void listSortedWithComparatorDownPrioritizesInternalIdentifier() {
        List<AccountIdentifier> unsorted = Lists.newArrayList(
                new SwedishIdentifier("6769952279428"),
                new SwedishIdentifier("33008607015537"),
                new SwedishSHBInternalIdentifier("952279428"),
                new SwedishIdentifier("33008210303999"));

        ImmutableList<AccountIdentifier> prioritized = FluentIterable
                .from(unsorted)
                .toSortedList(AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW);

        assertThat(prioritized).hasSameSizeAs(unsorted);
        assertThat(prioritized).containsAll(unsorted);
        assertThat(prioritized.get(prioritized.size() - 1)).isEqualTo(new SwedishSHBInternalIdentifier("952279428"));
    }

    @Test
    public void listSortedWithComparatorDownPrioritizesNonValidIdentifier() {
        List<AccountIdentifier> unsorted = Lists.newArrayList(
                new SwedishIdentifier("6769952279428"),
                new SwedishIdentifier("33008607015537"),
                new NonValidIdentifier(null),
                new SwedishIdentifier("33008210303999"));

        ImmutableList<AccountIdentifier> prioritized = FluentIterable
                .from(unsorted)
                .toSortedList(AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW);

        assertThat(prioritized).hasSameSizeAs(unsorted);
        assertThat(prioritized).containsAll(unsorted);
        assertThat(prioritized.get(prioritized.size() - 1)).isEqualTo(new NonValidIdentifier(null));
    }

    @Test
    public void listSortedWithComparatorPrioritizesInternalBeforeNonValid() {
        List<AccountIdentifier> unsorted = Lists.newArrayList(
                new NonValidIdentifier(null),
                new SwedishSHBInternalIdentifier("952279428"));

        ImmutableList<AccountIdentifier> prioritized = FluentIterable
                .from(unsorted)
                .toSortedList(AccountIdentifierComparator.IDENTIFIERS_PRIORITIZED_HIGH_TO_LOW);

        assertThat(prioritized).hasSameSizeAs(unsorted);
        assertThat(prioritized).containsAll(unsorted);
        assertThat(prioritized.get(0)).isEqualTo(new SwedishSHBInternalIdentifier("952279428"));
        assertThat(prioritized.get(1)).isEqualTo(new NonValidIdentifier(null));
    }
}
