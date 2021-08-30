package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

@AllArgsConstructor
public class CmcicRequestValuesProvider {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(Signature.DATE_FORMAT, Locale.US);

    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    String randomUuid() {
        return randomValueGenerator.getUUID().toString();
    }

    String getServerTime() {
        return localDateTimeSource.now().atZone(ZoneId.of(Signature.TIMEZONE)).format(FORMATTER);
    }
}
