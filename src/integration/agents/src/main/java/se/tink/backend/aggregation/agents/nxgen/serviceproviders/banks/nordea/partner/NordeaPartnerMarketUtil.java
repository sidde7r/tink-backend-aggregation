package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
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
            Account account, CredentialsRequest request, LocalDateTimeSource dateTimeSource) {

        Optional<Date> certainDate =
                request.getAccounts().stream()
                        .filter(
                                a ->
                                        !a.isClosed()
                                                && account.isUniqueIdentifierEqual(a.getBankId()))
                        .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                        .filter(Objects::nonNull)
                        .findFirst();

        if (certainDate.isPresent()) {
            log.info("Certain date for getPaginationStartDate: " + certainDate);
            return certainDate.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else return getPaginationDateYearBack(dateTimeSource);
    }

    public static LocalDate getPaginationDateYearBack(LocalDateTimeSource dateTimeSource) {
        return dateTimeSource.now(ZoneId.systemDefault()).toLocalDate().minus(1, ChronoUnit.YEARS);
    }

    public static LocalDate getStartDate(
            List<se.tink.backend.agents.rpc.Account> ac, LocalDateTimeSource dateTimeSource) {
        Date best =
                Date.from(
                        getPaginationDateYearBack(dateTimeSource)
                                .atStartOfDay()
                                .toInstant(ZoneOffset.UTC));
        if (ac.isEmpty() || ac.stream().anyMatch(account -> account.getCertainDate() == null)) {
            return getPaginationDateYearBack(dateTimeSource);
        }
        for (se.tink.backend.agents.rpc.Account account : ac) {
            Date certainDate = (account.getCertainDate());
            log.info("Certain date for getStartDate: " + certainDate);
            if (certainDate.compareTo(best) > 0) {
                best = certainDate;
            }
        }
        return best.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
