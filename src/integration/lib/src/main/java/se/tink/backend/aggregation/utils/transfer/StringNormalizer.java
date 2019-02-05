package se.tink.backend.aggregation.utils.transfer;

public interface StringNormalizer {
    String normalize(String string);
    String getUnchangedCharactersHumanReadable();
}
