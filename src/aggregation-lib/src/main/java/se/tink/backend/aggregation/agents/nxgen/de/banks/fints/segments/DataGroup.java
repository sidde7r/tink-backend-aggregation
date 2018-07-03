package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsEscape;

public class DataGroup {
    private final List<DataElement> elements = new ArrayList<>();

    public void addElement(DataElement element) {
        elements.add(element);
    }

    public void addElement(String data) {
        elements.add(new DataElement(data));
    }

    public void addElement(int data) {
        elements.add(new DataElement(data));
    }

    public String asEscapedString() {
        return elements.stream()
                .map(DataElement::asEscapedString)
                .map(FinTsEscape::escapeDataElement)
                .collect(Collectors.joining(FinTsConstants.SegData.ELEMENT_DELIMITER));
    }

    public String asString() {
        return elements.stream()
                .map(DataElement::asString)
                .collect(Collectors.joining(FinTsConstants.SegData.ELEMENT_DELIMITER));
    }

    public Optional<String> get(int index) {
        if (elements.size() <= index) {
            return Optional.empty();
        }
        return Optional.of(elements.get(index).asString());
    }
}