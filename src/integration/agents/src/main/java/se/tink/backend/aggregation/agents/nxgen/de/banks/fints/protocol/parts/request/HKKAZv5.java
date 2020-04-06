package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import java.time.LocalDate;
import lombok.Builder;
import lombok.NonNull;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Messages_Geschaeftsvorfaelle_2015-08-07_final_version.pdf
 * Page ?? This class represents older version of this segment than what is represented in the
 * provided document.
 */
@Builder
public class HKKAZv5 extends BaseRequestPart {
    private static final boolean ASK_ABOUT_ALL_ACCOUNTS = false;
    @NonNull private String accountNumber;
    private String subAccountNumber;
    @NonNull private String blz;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startingPoint;

    @Override
    protected void compile() {
        super.compile();
        addGroup()
                .element(accountNumber)
                .element(subAccountNumber)
                .element(Constants.COUNTRY_CODE)
                .element(blz);
        addGroup().element(ASK_ABOUT_ALL_ACCOUNTS);

        addGroup().element(startDate);
        addGroup().element(endDate);
        addGroup(); // Place for max number of entries

        addGroup().element(startingPoint);
    }
}
