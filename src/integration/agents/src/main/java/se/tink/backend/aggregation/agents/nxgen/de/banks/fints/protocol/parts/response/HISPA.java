package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Accessors(chain = true)
@NoArgsConstructor
@Getter
public class HISPA extends BaseResponsePart {

    @Data
    @Accessors(chain = true)
    public static class Detail {
        private Boolean isSepaAccount;
        private String iban;
        private String bic;
        private String accountNumber;
        private String subAccountNumber;
        private String countryCode;
        private String blz;
    }

    private List<Detail> accountDetails = new ArrayList<>();

    HISPA(RawSegment rawSegment) {
        super(rawSegment);
        for (int i = 1; i < rawSegment.getGroups().size(); i++) {
            RawGroup group = rawSegment.getGroup(i);
            Detail detail = new Detail();
            detail.isSepaAccount = group.getBoolean(0);
            detail.iban = group.getString(1);
            detail.bic = group.getString(2);
            detail.accountNumber = group.getString(3);
            detail.subAccountNumber = group.getString(4);
            detail.countryCode = group.getString(5);
            detail.blz = group.getString(6);
            accountDetails.add(detail);
        }
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Arrays.asList(1, 2, 3);
    }
}
