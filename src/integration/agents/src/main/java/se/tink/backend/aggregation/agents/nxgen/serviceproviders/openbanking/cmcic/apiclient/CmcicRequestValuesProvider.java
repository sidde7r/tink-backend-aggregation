package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

public class CmcicRequestValuesProvider {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(Signature.DATE_FORMAT, Locale.US);

    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;
    private final String organizationName;

    @SneakyThrows
    public CmcicRequestValuesProvider(
            RandomValueGenerator randomValueGenerator,
            LocalDateTimeSource localDateTimeSource,
            String base64EncodedCertificates) {
        this.randomValueGenerator = randomValueGenerator;
        this.localDateTimeSource = localDateTimeSource;
        this.organizationName = CertificateUtils.getOrganizationName(base64EncodedCertificates);
    }

    String randomUuid() {
        return randomValueGenerator.getUUID().toString();
    }

    String getServerTime() {
        return localDateTimeSource.now().atZone(ZoneId.of(Signature.TIMEZONE)).format(FORMATTER);
    }

    String getOrganizationName() {
        return organizationName;
    }
}
