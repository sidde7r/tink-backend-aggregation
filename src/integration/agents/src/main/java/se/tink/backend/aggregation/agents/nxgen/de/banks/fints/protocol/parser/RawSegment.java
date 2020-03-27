package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class RawSegment {

    private List<RawGroup> groups = new ArrayList<>();

    public void addGroup(RawGroup group) {
        groups.add(group);
    }

    /**
     * Check if segment represented by this object is proper.
     *
     * @return true when the segment has a header group, ie. a group of three elements, name, order
     *     number, version number
     */
    public boolean isProperSegment() {
        boolean groupPresentAndBigEnough = groups.size() > 0 && groups.get(0).size() >= 3;
        List<String> headerGroup = groups.get(0);
        return groupPresentAndBigEnough
                && headerGroup.get(0).matches("[A-Z]{4,}")
                && headerGroup.get(1).matches("\\d+")
                && headerGroup.get(2).matches("\\d+");
    }

    /**
     * Retrieve segment name. This method assumes that the rawSegment is proper.
     *
     * @return First element of first group.
     */
    public String getSegmentName() {
        return groups.get(0).get(0);
    }

    public List<RawGroup> getGroups() {
        return groups;
    }

    /**
     * Returns group of elements (can be just one element) from a given position in segment.
     *
     * @param index
     * @return Group of elements, represented by RawGroup (List of Strings with some extra
     *     functionality). Prevents out-of-bounds exception by returning dummy group.
     */
    public RawGroup getGroup(int index) {
        return (index < groups.size()) ? groups.get(index) : RawGroup.DUMMY;
    }
}
