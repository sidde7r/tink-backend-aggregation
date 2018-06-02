package se.tink.libraries.identity.model;

import org.joda.time.DateTime;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class IdentityTest {
    @Test
    public void testTaxDeclarationWhenNotSet() {
        Identity identity = new Identity();

        TaxDeclaration taxDeclaration = createTaxDeclaration(new DateTime(), new DateTime());

        identity.setTaxDeclarationIfMoreRecent(taxDeclaration);

        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(taxDeclaration);
    }

    @Test
    public void testTaxDeclarationUpdatedWhenLaterIsAvailable() {
        Identity identity = new Identity();

        TaxDeclaration firstTaxDeclaration = createTaxDeclaration(new DateTime(), new DateTime());
        TaxDeclaration secondTaxDeclaration = createTaxDeclaration(new DateTime().plusDays(1), new DateTime());

        identity.setTaxDeclarationIfMoreRecent(firstTaxDeclaration);
        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(firstTaxDeclaration);

        identity.setTaxDeclarationIfMoreRecent(secondTaxDeclaration);
        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(secondTaxDeclaration);
    }

    @Test
    public void testTaxDeclarationUpdatedWhenRegisteredDateIsNullAndLaterIsAvailable() {
        Identity identity = new Identity();

        TaxDeclaration firstTaxDeclaration = createTaxDeclaration(new DateTime(), new DateTime());
        TaxDeclaration secondTaxDeclaration = createTaxDeclaration(null, new DateTime().plusDays(1));

        identity.setTaxDeclarationIfMoreRecent(firstTaxDeclaration);
        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(firstTaxDeclaration);

        identity.setTaxDeclarationIfMoreRecent(secondTaxDeclaration);
        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(secondTaxDeclaration);
    }

    @Test
    public void testTaxDeclarationNotUpdatedWhenRegisteredDateIsNull() {
        Identity identity = new Identity();

        TaxDeclaration firstTaxDeclaration = createTaxDeclaration(new DateTime(), new DateTime());
        TaxDeclaration secondTaxDeclaration = createTaxDeclaration(null, new DateTime().minusDays(1));

        identity.setTaxDeclarationIfMoreRecent(firstTaxDeclaration);
        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(firstTaxDeclaration);

        identity.setTaxDeclarationIfMoreRecent(secondTaxDeclaration);
        assertThat(identity.getMostRecentTaxDeclaration()).isEqualTo(firstTaxDeclaration);
    }

    private static TaxDeclaration createTaxDeclaration(DateTime registered, DateTime createdDate) {
        return TaxDeclaration.of(100D, 200D, 300D, 400D, 2017, registered == null ? null : registered.toDate(),
                createdDate.toDate());
    }

    @Test
    public void testDebtWhenNotSet() {
        Identity identity = new Identity();

        OutstandingDebt debt = createDebt(new DateTime(), new DateTime());

        identity.setOutstandingDebtIfMoreRecent(debt);

        assertThat(identity.getOutstandingDebt()).isEqualTo(debt);
    }

    @Test
    public void testDebtUpdatedWhenLaterIsAvailable() {
        Identity identity = new Identity();

        OutstandingDebt firstDebt = createDebt(new DateTime(), new DateTime());
        OutstandingDebt secondDebt = createDebt(new DateTime().plusDays(1), new DateTime());

        identity.setOutstandingDebtIfMoreRecent(firstDebt);
        assertThat(identity.getOutstandingDebt()).isEqualTo(firstDebt);

        identity.setOutstandingDebtIfMoreRecent(secondDebt);
        assertThat(identity.getOutstandingDebt()).isEqualTo(secondDebt);
    }

    @Test
    public void testDebtUpdatedWhenRegisteredDateIsNullAndLaterIsAvailable() {
        Identity identity = new Identity();

        OutstandingDebt firstDebt = createDebt(new DateTime(), new DateTime());
        OutstandingDebt secondDebt = createDebt(null, new DateTime().plusDays(1));

        identity.setOutstandingDebtIfMoreRecent(firstDebt);
        assertThat(identity.getOutstandingDebt()).isEqualTo(firstDebt);

        identity.setOutstandingDebtIfMoreRecent(secondDebt);
        assertThat(identity.getOutstandingDebt()).isEqualTo(secondDebt);
    }

    @Test
    public void testDebtNotUpdatedWhenRegisteredDateIsNull() {
        Identity identity = new Identity();

        OutstandingDebt firstDebt = createDebt(new DateTime(), new DateTime());
        OutstandingDebt secondDebt = createDebt(null, new DateTime().minusDays(1));

        identity.setOutstandingDebtIfMoreRecent(firstDebt);
        assertThat(identity.getOutstandingDebt()).isEqualTo(firstDebt);

        identity.setOutstandingDebtIfMoreRecent(secondDebt);
        assertThat(identity.getOutstandingDebt()).isEqualTo(firstDebt);
    }

    private static OutstandingDebt createDebt(DateTime registered, DateTime createdDate) {
        return OutstandingDebt.of(100D, 10, registered == null ? null : registered.toDate(), createdDate.toDate());
    }
}
