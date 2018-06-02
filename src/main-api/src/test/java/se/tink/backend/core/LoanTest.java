package se.tink.backend.core;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class LoanTest {

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    Loan loan1;
    Loan loan2;
    Loan loan3;
    Loan loan4;

    @Before
    public void setUp() throws Exception {
        UUID account1 = UUID.randomUUID();

        loan1 = new Loan();
        loan1.setId(UUIDs.startOf(df.parse("2015-07-22").getTime()));
        loan1.setAccountId(account1);
        loan1.setBalance(-500000.0);
        loan1.setName("Hypotekslån");
        loan1.setInterest(0.0160);

        loan2 = new Loan();
        loan2.setId(UUIDs.startOf(df.parse("2015-08-22").getTime()));
        loan2.setAccountId(account1);
        loan2.setBalance(-500000.0);
        loan2.setName("Hypotekslån");
        loan2.setInterest(null);

        loan3 = new Loan();
        loan3.setId(UUIDs.startOf(df.parse("2015-09-22").getTime()));
        loan3.setAccountId(account1);
        loan3.setBalance(-500000.0);
        loan3.setName("Hypotekslån");
        loan3.setInterest(null);

        loan4 = new Loan();
        loan4.setId(UUIDs.startOf(df.parse("2015-10-22").getTime()));
        loan4.setAccountId(account1);
        loan4.setBalance(-499000.0);
        loan4.setName("Hypotekslån");
        loan4.setInterest(null);
    }

    @Test
    public void testIsUpdatedWithNullInterest() {
        // Don't add a null interest if previous row had interest
        assertFalse(loan2.hasUpdatedSince(loan1));
        // If both new and prev has null interest rate -- Danske bank case (don't store another equal null interest row)
        assertFalse(loan3.hasUpdatedSince(loan2));
        // If both new and prev has null interest rate but something else changed
        assertTrue(loan4.hasUpdatedSince(loan2));

        // Verify the backwards case, we didn't have interest rate, store new one
        assertTrue(loan1.hasUpdatedSince(loan2));
    }

    @Test
    public void testSorting() {

        List<Loan> loans = Lists.newArrayList(loan1, loan2, loan3, loan4);
        Collections.shuffle(loans);

        System.out.println("Shuffled: ");
        for(Loan l : loans) {
            System.out.println(df.format(UUIDs.unixTimestamp(l.getId())));
        }

        Ordering<Loan> natural = Ordering.natural();
        List<Loan> sorted = natural.sortedCopy(loans);
        System.out.println("Sorted: ");
        for(Loan l : sorted) {
            System.out.println(df.format(UUIDs.unixTimestamp(l.getId())));
        }

        // oldest is first
        assertEquals(sorted.get(0).getId(), natural.min(loans).getId());
        // most recent is last
        assertEquals(sorted.get(sorted.size() - 1).getId(), natural.max(loans).getId());
    }
}