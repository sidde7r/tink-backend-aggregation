package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import java.math.BigDecimal;
import java.util.ArrayList;
import lombok.EqualsAndHashCode;
import org.assertj.core.util.Strings;

@EqualsAndHashCode(callSuper = true)
public class RawGroup extends ArrayList<String> {

    static final RawGroup DUMMY = new RawGroup();

    public String getString(int index) {
        if (index >= this.size()) {
            return null;
        }
        if ("".equals(get(index))) {
            return null;
        }
        return get(index);
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

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || (this.size() == 1 && Strings.isNullOrEmpty(get(0)));
    }
}
