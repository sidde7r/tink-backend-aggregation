package se.tink.backend.aggregation.utils.transfer;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import com.google.common.primitives.Chars;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class StringNormalizerEnglishTest {
    public static class Nothing {
        @Test
        public void isNullSafe() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();
            assertThat(normalizer.normalize(null)).isEqualTo(null);
        }

        @Test
        public void isEmptyStringSafe() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();
            assertThat(normalizer.normalize("")).isEqualTo("");
        }

        @Test
        public void doesNotNormalizeSpaces() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();
            assertThat(normalizer.normalize(" \t \n")).isEqualTo(" \t \n");
        }
    }

    public static class ReplaceNonEnglishWithClosest {
        @Test
        public void lowercaseSwedish() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();

            assertThat(normalizer.normalize("replace åäö")).isEqualTo("replace aao");
        }

        @Test
        public void uppercaseSwedish() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();

            assertThat(normalizer.normalize("REPLACE ÅÄÖ")).isEqualTo("REPLACE AAO");
        }

        @Test
        public void additionalCharacters() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();

            assertThat(normalizer.normalize("ß æ Æ ø Ø")).isEqualTo("s a A o O");
        }

        @Test
        public void specialCharsWithoutRepresentationAreRemoved() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();

            assertThat(normalizer.normalize("test$^#Ω removed Ω Ω")).isEqualTo("test removed  ");
        }

        @Test
        public void aloneDiacriticIsStripped() {
            StringNormalizerEnglish normalizer = new StringNormalizerEnglish();

            assertThat(normalizer.normalize("a^~a")).isEqualTo("aa");
        }
    }

    public static class Whitelist {
        @Test
        public void lowercaseSwedish() {
            StringNormalizerEnglish normalizer =
                    new StringNormalizerEnglish(
                            Sets.newConcurrentHashSet(Chars.asList('å', 'ä', 'ö')));

            assertThat(normalizer.normalize("test åäö ÅÄÖ")).isEqualTo("test åäö AAO");
        }

        @Test
        public void uppercaseSwedish() {
            StringNormalizerEnglish normalizer =
                    new StringNormalizerEnglish(
                            Sets.newConcurrentHashSet(Chars.asList('Å', 'Ä', 'Ö')));

            assertThat(normalizer.normalize("test åäö ÅÄÖ")).isEqualTo("test aao ÅÄÖ");
        }

        @Test
        public void additionalCharacters() {
            StringNormalizerEnglish normalizer =
                    new StringNormalizerEnglish(
                            Sets.newConcurrentHashSet(Chars.asList('ß', 'æ', 'Æ')));

            assertThat(normalizer.normalize("ß æ Æ ø Ø")).isEqualTo("ß æ Æ o O");
        }

        @Test
        public void specialCharsWithoutRepresentation() {
            StringNormalizerEnglish normalizer =
                    new StringNormalizerEnglish(
                            Sets.newConcurrentHashSet(Chars.asList('$', '^', '#', '')));

            assertThat(normalizer.normalize("test$^#Ω Ω Ω")).isEqualTo("test$^#  ");
        }

        @Test
        public void getUnchangedCharactersHumanReadable_containsEnglishCharsAndWhiteList() {
            StringNormalizerEnglish normalizer =
                    new StringNormalizerEnglish(Sets.newHashSet('å', '#', '^'));

            String charactersHumanReadable = normalizer.getUnchangedCharactersHumanReadable();

            assertThat(charactersHumanReadable)
                    .matches("^a-z A-Z 0-9( [å#^]){3}$")
                    .contains("å")
                    .contains("#")
                    .contains("^");
        }
    }
}
