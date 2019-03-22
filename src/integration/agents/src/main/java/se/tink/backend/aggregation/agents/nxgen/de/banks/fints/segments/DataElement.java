package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsEscape;

public class DataElement {

    private final String data;

    DataElement(String data) {
        this.data = data;
    }

    DataElement(int data) {
        this.data = String.valueOf(data);
    }

    String asString() {
        return data;
    }

    String asEscapedString() {
        return FinTsEscape.escapeDataElement(data);
    }

    @Override
    public String toString() {
        return asString();
    }
}
