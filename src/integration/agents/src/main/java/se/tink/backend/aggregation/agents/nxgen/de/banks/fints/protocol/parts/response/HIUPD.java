package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

@Accessors(chain = true)
@NoArgsConstructor
@Getter
@Setter
public class HIUPD extends BaseResponsePart {

    @AllArgsConstructor
    @EqualsAndHashCode
    static class Limit {
        private String limitType;
        private String limitValue;
        private String limitCurrencyCode;
        private Integer limitDays;
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    static class AllowedBusinessOperation {
        private String operationName;
        private Integer minNumberSignatures;
    }

    private String accountNumber;
    private String subAccountNumber;
    private String countryCode;
    private String blz;

    private String iban;
    private String customerId;
    private Integer accountType;
    private String currencyCode;
    private String firstAccountHolder;
    private String secondAccountHolder;
    private String productName;

    private Limit accountLimit;

    private List<AllowedBusinessOperation> allowedBusinessOperations = new ArrayList<>();

    private String accountAdditionalInfo;

    HIUPD(RawSegment rawSegment) {
        super(rawSegment);
        RawGroup group = rawSegment.getGroup(1);
        accountNumber = group.getString(0);
        subAccountNumber = group.getString(1);
        countryCode = group.getString(2);
        blz = group.getString(3);

        iban = rawSegment.getGroup(2).getString(0);
        customerId = rawSegment.getGroup(3).getString(0);
        accountType = rawSegment.getGroup(4).getInteger(0);
        currencyCode = rawSegment.getGroup(5).getString(0);
        firstAccountHolder = rawSegment.getGroup(6).getString(0);
        secondAccountHolder = rawSegment.getGroup(7).getString(0);
        productName = rawSegment.getGroup(8).getString(0);

        group = rawSegment.getGroup(9);
        accountLimit =
                new Limit(
                        group.getString(0),
                        group.getString(1),
                        group.getString(2),
                        group.getInteger(3));

        for (int i = 10; i < 10 + 999; i++) {
            group = rawSegment.getGroup(i);
            if (group.isEmpty()) break; // TODO tests for this, and refactor
            allowedBusinessOperations.add(
                    new AllowedBusinessOperation(group.getString(0), group.getInteger(1)));
        }

        accountAdditionalInfo = rawSegment.getGroup(1009).getString(0);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(6);
    }

    public boolean isOperationSupported(SegmentType segmentType) {
        return allowedBusinessOperations.stream()
                .anyMatch(x -> segmentType.getSegmentType().equals(x.getOperationName()));
    }
}
