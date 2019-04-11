package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class AccountEntitySerializer extends StdSerializer<AccountEntity> {

    public AccountEntitySerializer() {
        this(null);
    }

    public AccountEntitySerializer(Class<AccountEntity> t) {
        super(t);
    }

    @Override
    public void serialize(AccountEntity value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField(composeKey("alias"), value.getAlias());
        gen.writeStringField(composeKey("description"), value.getDescription());
        gen.writeStringField(composeKey("availability"), value.getAvailability());
        gen.writeStringField(composeKey("owner"), value.getOwner());
        gen.writeStringField(composeKey("product"), value.getProduct());
        gen.writeStringField(composeKey("productType"), value.getProductType());
        gen.writeStringField(composeKey("entityCode"), value.getEntityCode());
        gen.writeStringField(composeKey("contractCode"), value.getContractCode());
        gen.writeStringField(composeKey("bic"), value.getBic());
        gen.writeStringField(composeKey("number"), value.getNumber());
        gen.writeStringField(composeKey("iban"), value.getIban());
        gen.writeStringField(composeKey("hashIban"), value.getHashIban());
        gen.writeStringField(composeKey("amount", "value"), value.getAmount().getValue());
        gen.writeStringField(composeKey("amount", "currency"), value.getAmount().getCurrency());
        gen.writeStringField(composeKey("numOwners"), String.valueOf(value.getNumOwners()));
        gen.writeStringField(composeKey("isOwner"), String.valueOf(value.isOwner()));
        gen.writeStringField(composeKey("isSBPManaged"), String.valueOf(value.isIberSecurities()));
        gen.writeStringField(
                composeKey("isIberSecurities"), String.valueOf(value.isIberSecurities()));
        gen.writeStringField(composeKey("joint"), value.getJoint());
        gen.writeStringField(composeKey("mobileWarning"), value.getMobileWarning());
        gen.writeStringField(
                composeKey("contractNumberFormatted"), value.getContractNumberFormatted());
        gen.writeStringField(composeKey("value"), value.getValue());
        gen.writeEndObject();
    }

    private String composeKey(String firstKey, String... otherKeys) {
        final String firstKeyPrefix = "account[";
        final String otherKeyPrefix = "[";
        final String keySuffix = "]";

        StringBuffer buffer = new StringBuffer();
        buffer.append(firstKeyPrefix);
        buffer.append(firstKey);
        buffer.append(keySuffix);

        for (String otherKey : otherKeys) {
            buffer.append(otherKeyPrefix);
            buffer.append(otherKey);
            buffer.append(keySuffix);
        }

        return buffer.toString();
    }
}
