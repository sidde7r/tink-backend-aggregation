package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.AccountProductTypes;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.account.AccountIdentifier.Type;

/*
 * From the SEB api documentation we have this information:
 * Privatkonto; A transaction account for everyday economy. It is possible to connect a card or
 * payment service to the account.
 * Enkla sparkonto; An account for savings. It is not possible to connect a card or payment service
 * to the account but it is possible to make money transfers from and to the account.
 * Personallönekonto; A transaction account for everyday economy.
 * Valutakonto; A transaction account in other currency than SEK. It is not possible to connect a
 * card or payment service to the account but it is possible to make money transfers from and to the
 * account.
 * Notariatkonto; An account connected to a SEB custody account. It is not possible to connect a
 * card or payment service to the account or to make money transfers from the account. It is
 * possible to make money transfers to the account. The account REST structure uses an anonymised
 * resource identifier to access account information. In order to query for transactions and
 * balances, a resource identifier must be fetched. Use the account list endpoint to get the
 * resource ids for all accounts. The resource id for each account will not change over time, so it
 * can be stored and reused for subsequent calls.*/
public class SebAccountPaymentCapabilityUtil {
    public static List<TransferDestinationPattern> inferTransferDestinationFromAccountProductType(
            Account account, Storage instanceStorage) {
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
        Map<String, String> accountProductTypeMap =
                instanceStorage
                        .get(SebConstants.Storage.ACCOUNT_PRODUCT_MAP, typeRef)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No account product map in storage."));
        return getTransferDestinationsForProductType(
                accountProductTypeMap.get(account.getAccountNumber()));
    }

    private static List<TransferDestinationPattern> getTransferDestinationsForProductType(
            String accountProductType) {
        switch (accountProductType.toLowerCase()) {
            case AccountProductTypes.PRIVAT_KONTO:
            case AccountProductTypes.PERSONALLONEKONTO:
                return Arrays.asList(
                        TransferDestinationPattern.createForMultiMatchAll(Type.SE_BG),
                        TransferDestinationPattern.createForMultiMatchAll(Type.SE_PG),
                        TransferDestinationPattern.createForMultiMatchAll(Type.IBAN),
                        TransferDestinationPattern.createForMultiMatchAll(Type.SE));
            case AccountProductTypes.ENKLA_SPARKONTO:
            case AccountProductTypes.VALUTAKONTO:
                return Arrays.asList(
                        TransferDestinationPattern.createForMultiMatchAll(Type.SE),
                        TransferDestinationPattern.createForMultiMatchAll(Type.IBAN));
            case AccountProductTypes.NOTARIATKONTO:
                return Collections.emptyList();
            default:
                LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
                        .warn("Unknown account product type: {}", accountProductType);
                return Collections.emptyList();
        }
    }
}
