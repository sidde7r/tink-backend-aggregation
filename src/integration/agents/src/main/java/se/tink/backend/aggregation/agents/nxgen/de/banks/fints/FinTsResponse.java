package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants.StatusCode.PIN_TEMP_BLOCKED;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsEscape;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsParser;

public class FinTsResponse {

    private String response;
    private List<String> segments;

    public FinTsResponse(String data) {

        this.segments = Arrays.asList(FinTsParser.splitSegments(data));
        this.response = FinTsParser.unwrapSegment(data);
    }

    public String findSegment(String name) {

        // First look at response
        String[] splits = this.response.split(FinTsConstants.SegData.ELEMENT_DELIMITER, 2);
        if (Objects.equals(splits[0], name)) {
            return this.response;
        }

        // Then look at segments
        List<String> found = this.findSegments(name);
        return found.size() == 0 ? "" : found.get(0);
    }

    public List<String> findSegments(String name) {

        List<String> found = new ArrayList<>();

        this.segments.forEach(
                s -> {
                    String[] splits = s.split(FinTsConstants.SegData.ELEMENT_DELIMITER, 2);
                    if (Objects.equals(splits[0], name)) {
                        found.add(s);
                    }
                });
        return found;
    }

    private String getSegmentIndex(int index, String segment) {
        List<String> splits = FinTsParser.getSegmentDataGroups(segment);
        if (splits.size() > index - 1) {
            return splits.get(index - 1);
        } else {
            return null;
        }
    }

    public String getDialogId() {
        String segment = this.findSegment(FinTsConstants.Segments.HNHBK);

        if (Strings.isNullOrEmpty(segment)) {
            throw new IllegalArgumentException("Invalid response, no HNHBK segment");
        }

        return this.getSegmentIndex(4, segment);
    }

    public String getSystemId() {
        String segment = this.findSegment(FinTsConstants.Segments.HISYN);
        return FinTsParser.getSystemId(segment);
    }

    private int getSegmentMaxVersion(String name) {
        int version = 3;
        List<String> segments = this.findSegments(name);
        for (String s : segments) {
            List<String> parts = FinTsParser.getSegmentDataGroups(s);
            List<String> segHeader = FinTsParser.getDataGroupElements(parts.get(0));
            int curver = Integer.valueOf(segHeader.get(2));
            if (curver > version) {
                version = curver;
            }
        }
        return version;
    }

    public boolean isSuccess() {
        Map<String, String> summary = this.getGlobalStatus();
        return summary.values().stream().noneMatch(status -> status.matches("^9.*"));
    }

    public boolean isAccountBlocked() {
        Map<String, String> summary = this.getLocalStatus();
        return summary.values().stream().anyMatch(status -> status.matches(PIN_TEMP_BLOCKED));
    }

    // <Message, StatusCode>
    public Map<String, String> getGlobalStatus() {

        String hirm = FinTsParser.getStatus(this.response);
        Map<String, String> result = new HashMap<>();
        List<String> splits = FinTsParser.getSegmentDataGroups(hirm);
        if (!splits.isEmpty()) {
            List<String> parts = splits.subList(1, splits.size());

            for (String part : parts) {
                List<String> dge = FinTsParser.getDataGroupElements(part);
                result.put(dge.get(2), dge.get(0));
            }
        }

        return result;
    }

    public Map<String, String> getLocalStatus() {

        Map<String, String> result = new HashMap<>();
        String seg = this.findSegment(FinTsConstants.Segments.HIRMS);
        List<String> splits = FinTsParser.getSegmentDataGroups(seg);
        if (!splits.isEmpty()) {
            List<String> parts = splits.subList(1, splits.size());

            for (String part : parts) {
                List<String> dge = FinTsParser.getDataGroupElements(part);
                result.put(dge.get(2), dge.get(0));
            }
        }

        return result;
    }

    public int getHKKAZMaxVersion() {
        return this.getSegmentMaxVersion(FinTsConstants.Segments.HIKAZS);
    }

    public int getHKSALMaxVersion() {
        return this.getSegmentMaxVersion(FinTsConstants.Segments.HISALS);
    }

    public List<String> getSupportedTanMechanisms() {
        List<String> segments = this.findSegments(FinTsConstants.Segments.HIRMS);

        for (String segment : segments) {
            List<String> degs = FinTsParser.getSegmentDataGroups(segment);
            degs = degs.subList(1, degs.size());
            for (String de : degs) {
                String[] splits = de.split("::", 2);
                if (FinTsConstants.StatusCode.TAN_VERSION.equals(splits[0])) {
                    return Arrays.asList(FinTsParser.getTanMech(splits[1]));
                }
            }
        }
        return Collections.emptyList();
    }

    public Map<String, String> getTouchDowns(FinTsRequest message) {
        Map<String, String> touchdown = new HashMap<>();
        for (FinTsSegment msgseg : message.getEncryptedSegments()) {
            String segment = this.findSegmentForReference(FinTsConstants.Segments.HIRMS, msgseg);
            if (!Strings.isNullOrEmpty(segment)) {
                List<String> parts = FinTsParser.getSegmentDataGroups(segment);
                parts.remove(0);
                for (String p : parts) {
                    List<String> psplit = FinTsParser.getDataGroupElements(p);
                    if (FinTsConstants.StatusCode.MORE_INFORMATION_AVAILABLE.equals(
                            psplit.get(0))) {
                        String td = psplit.get(3);
                        touchdown.put(msgseg.getType(), FinTsEscape.unescapeDataElement(td));
                    }
                }
            }
        }
        return touchdown;
    }

    private String findSegmentForReference(String name, FinTsSegment ref) {
        List<String> segs = this.findSegments(name);
        for (String seg : segs) {
            List<String> segSplit =
                    FinTsParser.getDataGroupElements(FinTsParser.getSegmentDataGroups(seg).get(0));
            if (Objects.equals(segSplit.get(3), String.valueOf(ref.getSegmentNumber()))) {
                return seg;
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return "FinTsResponse{" + "response='" + response + '\'' + '}';
    }
}
