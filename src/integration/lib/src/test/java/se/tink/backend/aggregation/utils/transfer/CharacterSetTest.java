package se.tink.backend.aggregation.utils.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class CharacterSetTest {
    @Test
    public void of() {
        CharacterSet characterSet = CharacterSet.of('a', 'b');

        assertThat(characterSet.get()).hasSize(2);
        assertThat(characterSet.get()).contains('a', 'b');
    }

    @Test
    public void ofWithString() {
        CharacterSet characterSet = CharacterSet.of("ab");

        assertThat(characterSet.get()).hasSize(2);
        assertThat(characterSet.get()).contains('a', 'b');
    }

    @Test
    public void unionProducesNewSetInstance() {
        CharacterSet first = CharacterSet.of('a', 'b');
        CharacterSet union = first.union('a', 'b');

        // The union set contains exactly same characters
        assertThat(first.get()).isEqualTo(union.get());

        // ...but it's new instances of the ImmutableSet and CharacterSet
        assertThat(first).isNotSameAs(union);
        assertThat(first.get()).isNotSameAs(union.get());
    }

    @Test
    public void unionWithCharacters() {
        CharacterSet characterSet = CharacterSet.of('a', 'b').union('a', 'c');

        assertThat(characterSet.get()).hasSize(3);
        assertThat(characterSet.get()).contains('a', 'b', 'c');
    }

    @Test
    public void unionWithOtherCharacterSet() {
        CharacterSet first = CharacterSet.of('a', 'b');
        CharacterSet second = CharacterSet.of('b', 'c', 'd');

        ImmutableSet<Character> union = first.union(second).get();

        assertThat(union).hasSize(4);
        assertThat(union).contains('a', 'b', 'c', 'd');
    }

    @Test
    public void unionWithString() {
        CharacterSet first = CharacterSet.of('a', 'b');
        String second = "bcd";

        ImmutableSet<Character> union = first.union(second).get();

        assertThat(union).hasSize(4);
        assertThat(union).contains('a', 'b', 'c', 'd');
    }
}
