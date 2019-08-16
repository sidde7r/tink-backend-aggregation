package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BankinterTransactionEntity extends TransactionEntity {
    private static Pattern DESCRIPTION_DATE_PATTERN =
            Pattern.compile("/TXT/[DH]\\|(?<description>.+)");

    @Override
    @JsonIgnore
    public String getDescription() {
        final Matcher matcher = DESCRIPTION_DATE_PATTERN.matcher(remittanceInformationUnstructured);
        if (matcher.find()) {
            return matcher.group("description");
        } else {
            return remittanceInformationUnstructured;
        }
    }
}
