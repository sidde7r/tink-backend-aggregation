package se.tink.backend.agents.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountPartyAddressType;

@Data
@AllArgsConstructor
public class AccountPartyAddress {
    private AccountPartyAddressType addressType;
    private String street;
    private String postalCode;
    private String city;
    private String country;
}
