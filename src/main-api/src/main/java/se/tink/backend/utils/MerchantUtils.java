package se.tink.backend.utils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import se.tink.backend.core.Creatable;
import se.tink.backend.core.Location;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.Place;
import se.tink.backend.core.StringStringPair;

public class MerchantUtils {
    public static Merchant createMerchant(Place place, MerchantSources source) {
        Merchant merchant = new Merchant();
        merchant.setCreated(new Date());

        return mergePlaceWithMerchant(merchant, place, source);
    }

    public static List<Merchant> createMerchants(List<Place> places, final MerchantSources source) {
        return Lists.newArrayList(Iterables.transform(places, place -> createMerchant(place, source)));
    }

    public static List<Merchant> createMerchantsFromAutocomplete(List<StringStringPair> places,
            final MerchantSources source) {
        return Lists.newArrayList(Iterables.transform(places, new Function<StringStringPair, Merchant>() {
            @Nullable
            @Override
            public Merchant apply(StringStringPair input) {
                return createMerchantFromAutocomplete(input, source);
            }
        }));
    }

    public static Merchant mergePlaceWithMerchant(Merchant merchant, Place place, MerchantSources source) {

        Merchant copy = new Merchant();

        BeanUtils.copyProperties(merchant, copy, Creatable.class);

        copy.setName(place.getName());
        copy.setFormattedAddress(place.getAddress());
        copy.setPhoneNumber(place.getPhoneNumber());
        copy.setPhotoAttributions(place.getPhotoAttributions());
        copy.setPhotoReference(place.getPhotoReference());
        copy.setPostalCode(place.getPostalCode());
        copy.setReference(place.getPlaceId());
        copy.setSource(source);
        copy.setTypes(place.getTypes());
        copy.setWebsite(place.getWebsite());

        Location location = place.getLocation();

        if (location != null) {
            copy.setAddress(location.getAddress());
            copy.setCity(location.getCity());
            copy.setCoordinates(location.getCoordinate());
            copy.setCountry(location.getCountry());
        }

        return copy;
    }

    /**
     * Create a merchant from google auto complete
     *
     * @param input  Key: PlaceId
     *               Value: String formatted as [establishment name, address] i.e. "Vapiano, Sturegatan, Östermalm,
     *               Stockholm, Sverige"
     * @param source
     * @return
     */
    public static Merchant createMerchantFromAutocomplete(StringStringPair input, MerchantSources source) {
        String placeId = input.getKey();
        String value = input.getValue();

        Merchant merchant = new Merchant();
        merchant.setCreated(new Date());
        merchant.setSource(source);

        merchant.setReference(placeId);

        int merchantNameIndex = value.indexOf(',');

        if (merchantNameIndex == -1) {
            // Case that shouldn't happen if search has been made on establishments
            merchant.setName(value);
        } else {
            merchant.setName(value.substring(0, merchantNameIndex).trim());
            merchant.setFormattedAddress(value.substring(merchantNameIndex + 1, value.length()).trim());
        }

        return merchant;
    }
}
