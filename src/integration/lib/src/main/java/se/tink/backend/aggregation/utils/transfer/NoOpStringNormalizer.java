package se.tink.backend.aggregation.utils.transfer;

public class NoOpStringNormalizer implements StringNormalizer {

    @Override
    public String normalize(String string) {
        return string;
    }

    @Override
    public String getUnchangedCharactersHumanReadable() {
        return "a-z A-Z 0-9";
    }
}
