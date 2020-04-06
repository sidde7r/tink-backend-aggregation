package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Messages_Geschaeftsvorfaelle_2015-08-07_final_version.pdf
 * Page 91
 */
@Getter
public class HICAZ extends BaseResponsePart {

    private String camtFormat;
    private List<String> camtFiles = new ArrayList<>();

    HICAZ(RawSegment rawSegment) {
        super(rawSegment);
        camtFormat = rawSegment.getGroup(2).getString(0);
        camtFiles.addAll(rawSegment.getGroup(3).asList());
        camtFiles.addAll(rawSegment.getGroup(4).asList());
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(1);
    }
}
