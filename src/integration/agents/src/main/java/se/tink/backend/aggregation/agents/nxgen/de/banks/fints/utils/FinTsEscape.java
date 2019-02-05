package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;

// Escape strings
// Ref:  https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Formals_2017-05-11_final_version.pdf
// Section  H.1.1
public class FinTsEscape {
    private final static String ESCAPE_CHAR = "?";

    public static String escapeDataElement(String s) {
        return s.replace(ESCAPE_CHAR, ESCAPE_CHAR + ESCAPE_CHAR)
                .replace(FinTsConstants.SegData.ELEMENT_DELIMITER,
                        ESCAPE_CHAR + FinTsConstants.SegData.ELEMENT_DELIMITER);
    }

    public static String escapeDataGroup(String s) {
        return s.replace(FinTsConstants.SegData.GROUP_DELIMITER, ESCAPE_CHAR + FinTsConstants.SegData.GROUP_DELIMITER)
                .replace(FinTsConstants.SegData.SEGMENT_DELIMITED,
                        ESCAPE_CHAR + FinTsConstants.SegData.SEGMENT_DELIMITED);
    }

    public static String unescapeDataElement(String s) {
        return s.replace(ESCAPE_CHAR + ESCAPE_CHAR, ESCAPE_CHAR)
                .replace(ESCAPE_CHAR + FinTsConstants.SegData.SEGMENT_DELIMITED,
                        FinTsConstants.SegData.SEGMENT_DELIMITED)
                .replace(ESCAPE_CHAR + FinTsConstants.SegData.GROUP_DELIMITER, FinTsConstants.SegData.GROUP_DELIMITER)
                .replace(ESCAPE_CHAR + FinTsConstants.SegData.ELEMENT_DELIMITER,
                        FinTsConstants.SegData.ELEMENT_DELIMITER);
    }

}