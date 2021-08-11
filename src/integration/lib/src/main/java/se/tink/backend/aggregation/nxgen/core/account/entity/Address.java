package se.tink.backend.aggregation.nxgen.core.account.entity;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountPartyAddressType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class Address {
    private final AccountPartyAddressType addressType;
    private final String street;
    private final String postalCode;
    private final String city;
    private final String country;

    public static Address of(
            AccountPartyAddressType addressType,
            String street,
            String postalCode,
            String city,
            String country) {
        Preconditions.checkNotNull(addressType, "Address Type must not be null");
        return new Address(addressType, street, postalCode, city, country);
    }
}
