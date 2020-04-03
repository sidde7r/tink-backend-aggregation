package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class HIPINS extends BaseResponsePart {

    private Integer maxNumberOfTasks;
    private Integer minNumberSignatures;
    private Integer securityClass;

    private Integer minPinLength;
    private Integer maxPinLength;
    private Integer maxTanLength;

    private String userIdFieldText;
    private String customerIdFieldText;

    private List<Pair<String, Boolean>> operations = new ArrayList<>();

    public HIPINS(RawSegment rawSegment) {
        super(rawSegment);
        maxNumberOfTasks = rawSegment.getGroup(1).getInteger(0);
        minNumberSignatures = rawSegment.getGroup(2).getInteger(0);
        securityClass = rawSegment.getGroup(3).getInteger(0);

        RawGroup rawGroup = rawSegment.getGroup(4);
        minPinLength = rawGroup.getInteger(0);
        maxPinLength = rawGroup.getInteger(1);
        maxTanLength = rawGroup.getInteger(2);
        userIdFieldText = rawGroup.getString(3);
        customerIdFieldText = rawGroup.getString(4);

        for (int i = 5; i < rawGroup.size(); i += 2) {
            operations.add(Pair.of(rawGroup.getString(i), rawGroup.getBoolean(i + 1)));
        }
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(1);
    }
}
