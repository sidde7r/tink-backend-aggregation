package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;

public class HKSPA extends FinTsSegment {

    private String accountNo;
    private String subaccFeature;
    private String blz;

    public HKSPA(int segmentNumber, String accountNo, String subaccFeature, String blz) {
        super(segmentNumber);

        if (!Strings.isNullOrEmpty(accountNo)) {
            addDataGroup(String.join(":", accountNo, subaccFeature, FinTsConstants.SegData.COUNTRY_CODE, blz));
        }

        this.accountNo = accountNo;
        this.subaccFeature = subaccFeature;
        this.blz = blz;

    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getSubaccFeature() {
        return subaccFeature;
    }

    public String getBlz() {
        return blz;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKSPA;
    }
}
