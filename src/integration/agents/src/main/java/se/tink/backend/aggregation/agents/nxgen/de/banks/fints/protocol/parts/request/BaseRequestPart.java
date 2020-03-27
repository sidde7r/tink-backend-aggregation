package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.FinTsParser;

public abstract class BaseRequestPart {

    protected int segmentPosition = Constants.UNINITIALIZED_SEGMENT_POSITION;
    private List<List<String>> compiledData = new ArrayList<>();

    public String getSegmentName() {
        return this.getClass().getSimpleName().split("v")[0];
    }

    public int getSegmentVersion() {
        return Integer.parseInt(this.getClass().getSimpleName().split("v")[1]);
    }

    public int getSegmentPosition() {
        return segmentPosition;
    }

    protected void compile() {
        this.compiledData.clear();
        addHeaderGroup();
    }

    private void addHeaderGroup() {
        addGroup()
                .element(getSegmentName())
                .element(getSegmentPosition())
                .element(getSegmentVersion());
    }

    public String toFinTsFormat() {
        compile();
        return compiledData.stream()
                .map(
                        group ->
                                String.join(FinTsParser.ELEMENT_DELIMITER, group)
                                        .replaceAll(":*$", ""))
                .collect(Collectors.joining(FinTsParser.ELEMENT_GROUP_DELIMITER))
                .replaceAll("\\+*$", "");
    }

    protected SegmentPositionCounter assignSegmentPosition(SegmentPositionCounter counter) {
        this.segmentPosition = counter.getAndIncrement();
        return counter;
    }

    // Helper methods for compile methods
    protected RawGroup addGroup() {
        RawGroup rawGroup = new RawGroup();
        compiledData.add(rawGroup.elements);
        return rawGroup;
    }

    protected static class RawGroup {
        private List<String> elements = new ArrayList<>();

        public RawGroup element() {
            return element("");
        }

        public RawGroup element(String value) {
            elements.add(value == null ? "" : FinTsParser.escape(value));
            return this;
        }

        public RawGroup element(Integer value) {
            elements.add(value == null ? "" : String.valueOf(value));
            return this;
        }

        public RawGroup element(Boolean value) {
            if (value == null) {
                elements.add("");
            } else {
                elements.add(value ? "J" : "N");
            }
            return this;
        }

        public RawGroup element(byte[] value) {
            elements.add(value == null ? "" : "@" + value.length + "@" + new String(value));
            return this;
        }

        public RawGroup element(BigDecimal value) {
            if (value == null) elements.add("");
            else {
                String partial = value.stripTrailingZeros().toPlainString().replace(".", ",");
                elements.add(partial.contains(",") ? partial : partial + ",");
            }
            return this;
        }

        public RawGroup element(LocalDate value) {
            elements.add(
                    value == null ? "" : value.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            return this;
        }

        public RawGroup element(LocalTime value) {
            elements.add(value == null ? "" : value.format(DateTimeFormatter.ofPattern("HHmmss")));
            return this;
        }
    }
}
