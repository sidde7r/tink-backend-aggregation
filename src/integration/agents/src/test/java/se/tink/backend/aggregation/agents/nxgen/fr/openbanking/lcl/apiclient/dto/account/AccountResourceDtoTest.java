package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account;

import org.junit.Assert;
import org.junit.Test;

public class AccountResourceDtoTest {

    @Test
    public void
            shouldReturnCreditCardIdentifierBuildFromLinkedAccountNumberAndLast4CharsFromCreditCardNumber() {
        // given
        AccountResourceDto dto = new AccountResourceDto();
        dto.setLinkedAccount("07779000540G");
        AccountIdentificationDto accountIdentificationDto = new AccountIdentificationDto();
        GenericIdentificationDto genericIdentificationDto = new GenericIdentificationDto();
        genericIdentificationDto.setIdentification("XXXX XXXX XXXX X530");
        accountIdentificationDto.setOther(genericIdentificationDto);
        dto.setAccountId(accountIdentificationDto);

        // when
        String result = dto.creditCardIdentifier();

        // then
        Assert.assertEquals("07779000540G" + "X530", result);
    }
}
