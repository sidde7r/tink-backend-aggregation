package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Messages_Geschaeftsvorfaelle_2015-08-07_final_version.pdf
 * Page 93
 */
@Getter
public class TransactionsCamtBankConfig extends BaseResponsePart {

    private Integer maxNumberOfTasks;
    private Integer minNumberSignatures;
    private Integer securityClass;

    private Integer storagePeriod;
    private Boolean canLimitNumberOfEntries;
    private Boolean canQueryAboutAllAcounts;
    private List<String> supportedCamtFormats = new ArrayList<>();

    TransactionsCamtBankConfig(RawSegment rawSegment) {
        super(rawSegment);
        maxNumberOfTasks = rawSegment.getGroup(1).getInteger(0);
        minNumberSignatures = rawSegment.getGroup(2).getInteger(0);
        securityClass = rawSegment.getGroup(3).getInteger(0);

        RawGroup rawGroup = rawSegment.getGroup(4);
        storagePeriod = rawGroup.getInteger(0);
        canLimitNumberOfEntries = rawGroup.getBoolean(1);
        canQueryAboutAllAcounts = rawGroup.getBoolean(2);
        supportedCamtFormats.addAll(rawGroup.slice(3, rawGroup.size()));
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(1);
    }
}
