package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@SuppressWarnings("ConstantConditions")
public class IdModuleTest {

    @Test(expected = IllegalArgumentException.class)
    public void missingUniqueId() {
        IdModule.builder()
                .withUniqueIdentifier(null)
                .withAccountNumber(null)
                .withAccountName(null)
                .addIdentifier(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingSanitizedUniqueId() {
        IdModule.builder()
                .withUniqueIdentifier("-?--  ")
                .withAccountNumber(null)
                .withAccountName(null)
                .addIdentifier(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void missingAccountNo() {
        IdModule.builder()
                .withUniqueIdentifier("LegitUniqueID")
                .withAccountNumber(null)
                .withAccountName(null)
                .addIdentifier(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void missingAccountName() {
        IdModule.builder()
                .withUniqueIdentifier("LegitUniqueID")
                .withAccountNumber("1234")
                .withAccountName(null)
                .addIdentifier(null)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void nullIdentifier() {
        IdModule.builder()
                .withUniqueIdentifier("LegitUniqueID")
                .withAccountNumber("1234")
                .withAccountName("Kalle Kula")
                .addIdentifier(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateIdentifier() {
        SwedishIdentifier identifier = new SwedishIdentifier("123456789");

        IdModule.builder()
                .withUniqueIdentifier("LegitUniqueID")
                .withAccountNumber("1234")
                .withAccountName("Kalle Kula")
                .addIdentifier(identifier)
                .addIdentifier(identifier)
                .build();
    }

    @Test
    public void workingBuild() {
        String accountName = "Kalle Kula";
        String uniqueId = "LegitUniqueID";
        String accountNumber = "1234";
        SwedishIdentifier identifier = new SwedishIdentifier("123456789");
        String productName = "Superkonto";

        IdModule id =
                IdModule.builder()
                        .withUniqueIdentifier(uniqueId)
                        .withAccountNumber(accountNumber)
                        .withAccountName(accountName)
                        .addIdentifier(identifier)
                        .setProductName(productName)
                        .build();

        assertEquals(uniqueId, id.getUniqueId());
        assertEquals(accountNumber, id.getAccountNumber());
        assertEquals(accountName, id.getAccountName());
        assertTrue(id.getIdentifiers().contains(identifier));
        assertEquals(1, id.getIdentifiers().size());
    }
}
