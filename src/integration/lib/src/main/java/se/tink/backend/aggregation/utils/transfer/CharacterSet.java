package se.tink.backend.aggregation.utils.transfer;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.ArrayUtils;

public class CharacterSet {
    public static final CharacterSet SWEDISH_AAO = CharacterSet.of('å', 'ä', 'ö', 'Å', 'Ä', 'Ö');

    private final ImmutableSet<Character> characters;

    private CharacterSet(ImmutableSet<Character> characters) {
        this.characters = characters;
    }

    public static CharacterSet of(Character... characters) {
        Preconditions.checkArgument(characters != null && characters.length > 0);

        return new CharacterSet(ImmutableSet.copyOf(characters));
    }

    public static CharacterSet of(String characterString) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(characterString));

        Character[] characters = ArrayUtils.toObject(characterString.toCharArray());

        return new CharacterSet(ImmutableSet.copyOf(characters));
    }

    public CharacterSet union(Character... characters) {
        Preconditions.checkArgument(characters != null && characters.length > 0);

        return new CharacterSet(
                ImmutableSet.<Character>builder().addAll(this.characters).add(characters).build());
    }

    public CharacterSet union(String characterString) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(characterString));

        Character[] characters = ArrayUtils.toObject(characterString.toCharArray());

        return union(characters);
    }

    public CharacterSet union(CharacterSet characterSet) {
        Preconditions.checkNotNull(characterSet);

        return new CharacterSet(
                ImmutableSet.<Character>builder()
                        .addAll(this.characters)
                        .addAll(characterSet.characters)
                        .build());
    }

    public ImmutableSet<Character> get() {
        return characters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CharacterSet that = (CharacterSet) o;

        return Objects.equal(this.characters, that.characters);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(characters);
    }
}
