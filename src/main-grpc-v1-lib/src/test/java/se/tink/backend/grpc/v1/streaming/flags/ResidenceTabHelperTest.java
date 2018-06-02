package se.tink.backend.grpc.v1.streaming.flags;

import com.google.common.collect.Lists;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.main.controllers.IdentityServiceController;
import se.tink.libraries.identity.model.Address;
import se.tink.libraries.identity.model.Identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResidenceTabHelperTest {
    @Mock
    private
    IdentityServiceController identityServiceController;

    @Test
    public void testDanishMarket() {
        User user = new User();

        user.setProfile(new UserProfile());
        user.getProfile().setMarket("DK");

        ResidenceTabHelper residenceTabHelper = new ResidenceTabHelper(identityServiceController);

        assertThat(residenceTabHelper.shouldDisplayResidenceTab(user, Lists.newArrayList())).isFalse();
    }

    @Test
    public void testZeroAccounts() {
        User user = new User();

        user.setProfile(new UserProfile());
        user.getProfile().setMarket("SE");

        ResidenceTabHelper residenceTabHelper = new ResidenceTabHelper(identityServiceController);

        assertThat(residenceTabHelper.shouldDisplayResidenceTab(user, Lists.newArrayList())).isFalse();
    }

    @Test
    public void testNoLoansAccount() {
        User user = new User();

        user.setProfile(new UserProfile());
        user.getProfile().setMarket("SE");

        Account account = new Account();
        account.setType(AccountTypes.CHECKING);

        ResidenceTabHelper residenceTabHelper = new ResidenceTabHelper(identityServiceController);

        assertThat(residenceTabHelper.shouldDisplayResidenceTab(user, Lists.newArrayList(account))).isFalse();
    }

    @Test
    public void testNoIdentity() {
        User user = new User();

        user.setProfile(new UserProfile());
        user.getProfile().setMarket("SE");

        Account account = new Account();
        account.setType(AccountTypes.LOAN);

        when(identityServiceController.getIdentityState(any())).thenReturn(Optional.empty());

        ResidenceTabHelper residenceTabHelper = new ResidenceTabHelper(identityServiceController);

        assertThat(residenceTabHelper.shouldDisplayResidenceTab(user, Lists.newArrayList(account))).isFalse();
    }

    @Test
    public void testNoAddress() {
        User user = new User();

        user.setProfile(new UserProfile());
        user.getProfile().setMarket("SE");

        Account account = new Account();
        account.setType(AccountTypes.LOAN);

        Identity identity = new Identity();
        identity.setAddress(null);

        when(identityServiceController.getIdentityState(any())).thenReturn(Optional.of(identity));

        ResidenceTabHelper residenceTabHelper = new ResidenceTabHelper(identityServiceController);

        assertThat(residenceTabHelper.shouldDisplayResidenceTab(user, Lists.newArrayList(account))).isFalse();
    }

    @Test
    public void testSwedishUserWithLoanAndAddress() {
        User user = new User();

        user.setProfile(new UserProfile());
        user.getProfile().setMarket("SE");

        Account account = new Account();
        account.setType(AccountTypes.LOAN);

        Identity identity = new Identity();
        identity.setAddress(new Address("Pippi Långstrumps gata", "16870", "Bromma", "Stockholm"));

        when(identityServiceController.getIdentityState(any())).thenReturn(Optional.of(identity));

        ResidenceTabHelper residenceTabHelper = new ResidenceTabHelper(identityServiceController);

        assertThat(residenceTabHelper.shouldDisplayResidenceTab(user, Lists.newArrayList(account))).isTrue();
    }
}
