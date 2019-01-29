package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Strings;
import java.util.List;
import org.junit.Test;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class EInvoiceBodyTest {
    private static final String UUID = "2016-04-26-16.20.03.177833";

    private static final Catalog CATALOG = Catalog.getCatalog("sv_SE");

    @Test
    public void ensureToTinkTransfers_ReturnsAbsent_When_EInvoices_NotFound() {
        EInvoiceBody body = new EInvoiceBody();
        body.setEInvoices(null);
        List<Transfer> eInvoices = body.toTinkTransfers(CATALOG);

        assertTrue(eInvoices.isEmpty());

        List<EInvoiceEntity> eInvoiceEntities = Lists.newArrayList();
        body.setEInvoices(eInvoiceEntities);

        eInvoices = body.toTinkTransfers(CATALOG);

        assertTrue(eInvoices.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureToTinkTransfers_ReturnsAbsent_When_NoEInvoices_AreValid() {
        EInvoiceEntity eInvoiceEntity1 = new EInvoiceEntity();
        eInvoiceEntity1.setUuid(UUID);
        eInvoiceEntity1.setType("PaymentBg");
        eInvoiceEntity1.setAccountNumber(null);

        EInvoiceEntity eInvoiceEntity2 = new EInvoiceEntity();
        eInvoiceEntity2.setUuid(UUID);
        eInvoiceEntity2.setType("Transfer");
        eInvoiceEntity2.setAccountNumber("687-5496");

        List<EInvoiceEntity> eInvoiceEntities = Lists.newArrayList();
        eInvoiceEntities.add(eInvoiceEntity1);
        eInvoiceEntities.add(eInvoiceEntity2);

        EInvoiceBody body = new EInvoiceBody();
        body.setEInvoices(eInvoiceEntities);

        body.toTinkTransfers(CATALOG);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureToTinkTransfers_ReturnsPresent_When_AtleastOneIsValid() {
        EInvoiceEntity eInvoiceEntity1 = new EInvoiceEntity();
        eInvoiceEntity1.setType("PaymentBg");
        eInvoiceEntity1.setAccountNumber(null);
        assertFalse(isValid(eInvoiceEntity1));

        EInvoiceEntity eInvoiceEntity2 = new EInvoiceEntity();
        eInvoiceEntity2.setType("PaymentBg");
        eInvoiceEntity2.setAccountNumber("687-5496");
        eInvoiceEntity2.setUuid(UUID);
        assertTrue(isValid(eInvoiceEntity2));

        List<EInvoiceEntity> eInvoiceEntities = Lists.newArrayList();
        eInvoiceEntities.add(eInvoiceEntity1);
        eInvoiceEntities.add(eInvoiceEntity2);

        EInvoiceBody body = new EInvoiceBody();
        body.setEInvoices(eInvoiceEntities);

        body.toTinkTransfers(CATALOG);
    }

    private boolean isValid(EInvoiceEntity entity) {
        if (Strings.isNullOrEmpty(entity.getUuid())) {
            return false;
        }
        if (Strings.isNullOrEmpty(entity.getAccountNumber())) {
            return false;
        }
        return true;
    }
}
