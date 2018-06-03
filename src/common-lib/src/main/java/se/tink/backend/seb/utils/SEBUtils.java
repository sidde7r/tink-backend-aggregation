package se.tink.backend.seb.utils;

public class SEBUtils {
    public static String getDepotNumberForHolding(String depotNumber) {
        return depotNumber.replaceAll("^01[0]*", "");
    }
}
