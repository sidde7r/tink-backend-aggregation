package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.resources;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class GenericResponseTestData {

    public static final GenericResponse INVALID_KYC =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"KYC_INVALID\",\n"
                            + "      \"text\": \"Please direct customer to the online banking to fill KYC.\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse MISSING_BANK_AGREEMENT =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"MISSING_BANK_AGREEMENT\",\n"
                            + "      \"text\": \"Missing bank agreement error message\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);
}
