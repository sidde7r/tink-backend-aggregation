package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaPartnerMarketUtil {

    public static String getLocaleDescription(String market) {
        String locale = getCountry(market);
        if (locale.equalsIgnoreCase("NO")) {
            return "Transaksjon";
        }
        if (locale.equalsIgnoreCase("FI")) {
            return "Transaktio";
        }
        if (locale.equalsIgnoreCase("SE") || locale.equalsIgnoreCase("DK")) {
            return "Transaktion";
        } else {
            return "Transaction";
        }
    }

    public static String getCountry(String market) {
        return market.substring(market.length() - 2);
    }

    public static boolean isNorway(String market) {
        String country = getCountry(market);
        return country.equalsIgnoreCase("NO");
    }

    public static LocalDate getPaginationStartDate(
            Account account, CredentialsRequest request, AgentComponentProvider componentProvider) {

        Optional<Date> certainDate =
                request.getAccounts().stream()
                        .filter(a -> account.isUniqueIdentifierEqual(a.getBankId()))
                        .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                        .filter(Objects::nonNull)
                        .findFirst();

        if (certainDate.isPresent()) {
            return certainDate.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else return getPaginationDateYearBack(componentProvider);
    }

    private static LocalDate getPaginationDateYearBack(AgentComponentProvider componentProvider) {
        return componentProvider
                .getLocalDateTimeSource()
                .now(ZoneId.systemDefault())
                .toLocalDate()
                .minus(1, ChronoUnit.YEARS);
    }
}
