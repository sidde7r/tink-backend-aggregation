package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import lombok.Builder;
import lombok.NonNull;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Messages_Geschaeftsvorfaelle_2015-08-07_final_version.pdf
 * Page 64
 */
@Builder
public class GetBalanceV6 extends BaseRequestPart {

    private static final boolean ASK_ABOUT_ALL_ACCOUNTS = false;
    @NonNull private String accountNumber;
    private String subAccountNumber;
    @NonNull private String blz;

    @Override
    public String getSegmentName() {
        return SegmentType.HKSAL.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 6;
    }

    @Override
    protected void compile() {
        super.compile();
        addGroup()
                .element(accountNumber)
                .element(subAccountNumber)
                .element(Constants.COUNTRY_CODE)
                .element(blz);
        addGroup().element(ASK_ABOUT_ALL_ACCOUNTS);
    }
}
