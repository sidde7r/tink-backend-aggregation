package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
@EqualsAndHashCode(callSuper = true)
public class HITAB extends BaseResponsePart {

    private Integer tanUsageOption;
    private List<TanMedia> tanMediaList = new ArrayList<>();

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class TanMedia {
        private String tanMediumClass;
        private Integer tanMediumStatus;
        private String cardNumber;
        private String cardSequence;
        private Integer cardType;
        private String accountNumber;
        private String subAccountNumber;
        private String countryIdentifier;
        private String bankCode;
        private String validFrom;
        private String validUntil;
        private String tanListNumber;
        private String tanMediumName;
    }

    HITAB(RawSegment rawSegment) {
        super(rawSegment);
        tanUsageOption = rawSegment.getGroup(1).getInteger(0);
        for (RawGroup group : getTanMediaGroups(rawSegment)) {
            TanMedia tanMedia =
                    TanMedia.builder()
                            .tanMediumClass(group.getString(0))
                            .tanMediumStatus(group.getInteger(1))
                            .tanMediumName(group.getString(12))
                            .build();
            tanMediaList.add(tanMedia);
        }
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(4);
    }

    private List<RawGroup> getTanMediaGroups(RawSegment rawSegment) {
        final int TAN_MEDIA_START_IDX = 2;
        List<RawGroup> allGroups = rawSegment.getGroups();
        if (allGroups.size() < TAN_MEDIA_START_IDX) {
            return Collections.emptyList();
        }
        return allGroups.subList(TAN_MEDIA_START_IDX, allGroups.size());
    }
}
