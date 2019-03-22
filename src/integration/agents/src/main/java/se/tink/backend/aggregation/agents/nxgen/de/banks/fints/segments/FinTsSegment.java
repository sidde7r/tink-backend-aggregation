package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;

public abstract class FinTsSegment {

    private final boolean doEscapeData;
    private final int segmentNumber;
    private final List<FinTsSegment> segments = new ArrayList<>();

    final List<DataGroup> dataGroups = new ArrayList<>();

    public FinTsSegment(int segmentNumber) {
        this(segmentNumber, true);
    }

    public FinTsSegment(int segmentNumber, boolean doEscapeData) {
        this.segmentNumber = segmentNumber;
        this.doEscapeData = doEscapeData;
    }

    public int getSegmentNumber() {
        return segmentNumber;
    }

    public abstract int getVersion();

    public abstract String getType();

    protected void addDataGroup(String... elements) {
        DataGroup dataGroup = new DataGroup();

        for (String element : elements) {
            dataGroup.addElement(element);
        }

        dataGroups.add(dataGroup);
    }

    protected void addDataGroup(String element) {
        DataGroup dataGroup = new DataGroup();
        dataGroup.addElement(element);
        dataGroups.add(dataGroup);
    }

    protected void addDataGroup(int element) {
        addDataGroup(String.valueOf(element));
    }

    public void addDataGroup(DataGroup dataGroup) {
        dataGroups.add(dataGroup);
    }

    protected void clearData() {
        dataGroups.clear();
    }

    protected List<FinTsSegment> getSegments() {
        return segments;
    }

    private String getDataAsString() {
        String dataString;
        if (doEscapeData) {
            dataString =
                    dataGroups.stream()
                            .map(DataGroup::asEscapedString)
                            .collect(Collectors.joining(FinTsConstants.SegData.GROUP_DELIMITER));
        } else {
            dataString =
                    dataGroups.stream()
                            .map(DataGroup::asString)
                            .collect(Collectors.joining(FinTsConstants.SegData.GROUP_DELIMITER));
        }
        if (dataString.length() > 0) {
            dataString = FinTsConstants.SegData.GROUP_DELIMITER + dataString;
        }
        return dataString;
    }

    @Override
    public String toString() {
        return String.format(
                "%s:%s:%s%s%s",
                getType(),
                segmentNumber,
                getVersion(),
                getDataAsString(),
                FinTsConstants.SegData.SEGMENT_DELIMITED);
    }
}
