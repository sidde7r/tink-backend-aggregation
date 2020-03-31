package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import org.assertj.core.util.Strings;

@EqualsAndHashCode
public class RawGroup {

    static final RawGroup DUMMY = new RawGroup(Collections.emptyList());

    private List<String> data;

    public RawGroup(List<String> data) {
        this.data = Collections.unmodifiableList(data);
    }

    public String getString(int index) {
        if (index >= data.size()) {
            return null;
        }
        if ("".equals(data.get(index))) {
            return null;
        }
        return data.get(index);
    }

    public Boolean getBoolean(int index) {
        String element = getString(index);
        if (element == null) {
            return null;
        }
        if ("J".equals(element)) {
            return Boolean.TRUE;
        } else if ("N".equals(element)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Could not map to boolean: " + element);
    }

    public Integer getInteger(int index) {
        String element = getString(index);
        if (element == null) {
            return null;
        }
        try {
            return Integer.valueOf(element);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not map to integer: " + element);
        }
    }

    public BigDecimal getDecimal(int index) {
        String element = getString(index);
        if (element == null) {
            return null;
        }
        try {
            return new BigDecimal(element.replaceFirst(",$", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not map to decimal: " + element);
        }
    }

    public boolean isEmpty() {
        return data.isEmpty() || (data.size() == 1 && Strings.isNullOrEmpty(data.get(0)));
    }

    public int size() {
        return data.size();
    }

    public List<String> asList() {
        return data;
    }

    public List<String> slice(int fromIndex, int toIndex) {
        return data.subList(fromIndex, toIndex);
    }
}
