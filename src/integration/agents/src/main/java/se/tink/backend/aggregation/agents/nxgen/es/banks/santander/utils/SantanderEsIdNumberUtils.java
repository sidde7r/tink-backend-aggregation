package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils;

public class SantanderEsIdNumberUtils {
    /**
     * Return "N" for NIF ID numbers and "C" for NIE ID numbers. NIF numbers start with a digit
     * while NIE numbers start with a letter.
     */
    public static IdNumberTypes getIdNumberType(String username) {
        return startsWithDigit(username) ? IdNumberTypes.NIF : IdNumberTypes.NIE;
    }

    public static boolean startsWithDigit(String username) {
        return username.substring(0, 1).matches("[\\d]");
    }

    public enum IdNumberTypes {
        NIE,
        NIF,
        PASSPORT
    }
}
