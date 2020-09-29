package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DanskeConstants {

    public static final List<UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code>
            ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS = ImmutableList.of(BBAN, IBAN);
    public static final Pattern EXTRACT_ACCOUNT_NO_FROM_IBAN_PATTERN =
            Pattern.compile("(?:DK[0-9]{2}+3000)(\\d*)$");
    public static final int ACCOUNT_NO_MIN_LENGTH = 10;
    public static final int BRANCH_CODE_LENGTH = 4;
}
