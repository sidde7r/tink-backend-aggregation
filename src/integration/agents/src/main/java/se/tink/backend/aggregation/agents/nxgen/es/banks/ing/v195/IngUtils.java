package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public abstract class IngUtils {
    private static final AggregationLogger LOGGER = new AggregationLogger(IngConstants.class);

    public static final DateTimeFormatter BIRTHDAY_INPUT = DateTimeFormatter.ofPattern("ddMMyyyy");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Date toJavaLangDate(LocalDate localDate) {
        return new Date(localDate.atStartOfDay(IngConstants.ZONE_ID).toInstant().toEpochMilli());
    }

    public static Date toJavaLangDate(String dateAsString) {
        return toJavaLangDate(LocalDate.parse(dateAsString, DATE_FORMATTER));
    }

    public static void logUnknownProducts(List<Product> products) {

        products.stream().filter(
                product -> !IngConstants.AccountCategories.ALL_KNOWN_ACCOUNT_TYPES.contains(product.getType())
        ).forEach(product -> LOGGER.infoExtraLong(SerializationUtils.serializeToString(product),
                IngConstants.Logging.UNKNOWN_ACCOUNT_TYPE));
    }
}
