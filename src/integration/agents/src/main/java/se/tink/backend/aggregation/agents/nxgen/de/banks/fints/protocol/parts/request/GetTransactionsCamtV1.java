package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import java.time.LocalDate;
import lombok.Builder;
import lombok.NonNull;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Messages_Geschaeftsvorfaelle_2015-08-07_final_version.pdf
 * Page 90
 */
@Builder
public class GetTransactionsCamtV1 extends BaseRequestPart {

    private static final boolean ASK_ABOUT_ALL_ACCOUNTS = false;
    @NonNull private String iban;
    @NonNull private String bic;
    @NonNull private String accountNumber;
    private String subAccountNumber;
    @NonNull private String blz;
    @NonNull private String camtFormat;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startingPoint;

    @Override
    public String getSegmentName() {
        return SegmentType.HKCAZ.getSegmentName();
    }

    @Override
    public int getSegmentVersion() {
        return 1;
    }

    @Override
    protected void compile() {
        super.compile();
        addGroup()
                .element(iban)
                .element(bic)
                .element(accountNumber)
                .element(subAccountNumber)
                .element(Constants.COUNTRY_CODE)
                .element(blz);
        addGroup().element(camtFormat);
        addGroup().element(ASK_ABOUT_ALL_ACCOUNTS);

        addGroup().element(startDate);
        addGroup().element(endDate);
        addGroup(); // Place for max number of entries

        addGroup().element(startingPoint);
    }
}
