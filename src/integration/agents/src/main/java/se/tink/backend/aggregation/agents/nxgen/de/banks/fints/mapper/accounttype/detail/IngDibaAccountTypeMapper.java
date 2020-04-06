package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

@Slf4j
public class IngDibaAccountTypeMapper implements AccountTypeMapper {

    private static final Map<String, AccountTypes> TYPE_MAP = new HashMap<>();

    @Override
    public Optional<AccountTypes> getAccountTypeFor(HIUPD basicAccountInformation) {
        String productName = basicAccountInformation.getProductName().toUpperCase();
        AccountTypes accountType = TYPE_MAP.get(productName);
        if (accountType == null) {
            log.warn("Could not map product name: '{}' to any account type", productName);
        }
        return Optional.ofNullable(accountType);
    }

    static {
        TYPE_MAP.put("Extra-Konto".toUpperCase(), AccountTypes.SAVINGS);
        TYPE_MAP.put("Sparbrief".toUpperCase(), AccountTypes.SAVINGS);
        TYPE_MAP.put("Vl-Sparen".toUpperCase(), AccountTypes.SAVINGS);
        TYPE_MAP.put("Girokonto".toUpperCase(), AccountTypes.CHECKING);
        TYPE_MAP.put("Direkt-Depot".toUpperCase(), AccountTypes.INVESTMENT);
    }
}
