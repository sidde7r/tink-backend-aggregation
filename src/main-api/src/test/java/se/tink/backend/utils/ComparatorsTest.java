package se.tink.backend.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;


public class ComparatorsTest {
    @Test
    public void testNullAccountNumber() {
        Map<String, Credentials> credentialsById = Maps.newHashMap();
        
        Comparator<Account> comparator = Comparators.accountByFullName(credentialsById);
        
        Account a1 = new Account();
        a1.setName("A1");
        a1.setAccountNumber("123");
        
        Account a1Null = new Account();
        a1Null.setName("A1");
        
        Account a2 = new Account();
        a2.setName("A2");
        a2.setAccountNumber("123");
        
        Account a2Null = new Account();
        a2Null.setName("A2");
        
        assertThat(comparator.compare(a1, a1)).isEqualTo(0);
        
        assertThat(comparator.compare(a1, a2)).isLessThan(0);
        assertThat(comparator.compare(a2, a1)).isGreaterThan(0);
        
        assertThat(comparator.compare(a1, a1Null)).isGreaterThan(0);
        assertThat(comparator.compare(a1Null, a1)).isLessThan(0);
        
        assertThat(comparator.compare(a2, a2Null)).isGreaterThan(0);
        assertThat(comparator.compare(a2Null, a2)).isLessThan(0);
        
        assertThat(comparator.compare(a1Null, a2Null)).isLessThan(0);
        assertThat(comparator.compare(a2Null, a1Null)).isGreaterThan(0);
    }
}
