package se.tink.backend.aggregation.utils.transfer;

import java.util.Optional;
import com.google.common.collect.Sets;
import java.util.Set;

public class StringNormalizerSwedish extends StringNormalizerEnglish {
    private final Set<Character> nonSwedishWhiteList;

    public StringNormalizerSwedish() {
        super(CharacterSet.SWEDISH_AAO.get());
        this.nonSwedishWhiteList = createNonSwedishWhiteList(this.whiteListedCharacters);
    }

    public StringNormalizerSwedish(Character... whiteListedCharacters) {
        super(CharacterSet.SWEDISH_AAO.union(whiteListedCharacters).get());
        this.nonSwedishWhiteList = createNonSwedishWhiteList(this.whiteListedCharacters);
    }

    public StringNormalizerSwedish(String whiteListedCharacterString) {
        super(CharacterSet.SWEDISH_AAO.union(whiteListedCharacterString).get());
        this.nonSwedishWhiteList = createNonSwedishWhiteList(this.whiteListedCharacters);
    }

    /**
     * Used later for constructing human readable list of chars subject to normalization
     */
    private static Set<Character> createNonSwedishWhiteList(Optional<Set<Character>> whiteListedCharacters) {
        if (!whiteListedCharacters.isPresent()) {
            return Sets.newHashSet();
        }

        Set<Character> characters = Sets.newHashSet(whiteListedCharacters.get());
        characters.remove('å');
        characters.remove('ä');
        characters.remove('ö');
        characters.remove('Å');
        characters.remove('Ä');
        characters.remove('Ö');

        return characters;
    }

    @Override
    public String getUnchangedCharactersHumanReadable() {
        return createUnchangedCharactersHumanReadable("a-ö A-Ö 0-9", Optional.of(nonSwedishWhiteList));
    }
}
