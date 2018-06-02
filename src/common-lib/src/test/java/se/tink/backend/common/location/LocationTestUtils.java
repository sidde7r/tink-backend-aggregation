package se.tink.backend.common.location;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import java.util.Date;
import javax.annotation.Nullable;
import org.junit.Assert;
import se.tink.backend.common.location.transaction.DailyCityExistence;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.CityCoordinate;
import se.tink.backend.core.Coordinate;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserLocation;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static org.junit.Assert.assertEquals;

public class LocationTestUtils {

    public static void verifyCityLocationGuess (CityLocationGuess g, String city, float probability) {
        Assert.assertEquals(LocationResolution.CITY, g.getResolution());

        assertEquals(city, g.getCity());
        assertEquals(probability, g.getProbability(), 0.00001f);
    }

    public static void verifyCityLocationGuess (CityLocationGuess g, String city, float probability, LocationGuessType type) {
        Assert.assertEquals(LocationResolution.CITY, g.getResolution());
        Assert.assertEquals(type, g.getType());
        assertEquals(city, g.getCity());
        assertEquals(probability, g.getProbability(), 0.00001f);
    }

    public static Merchant createMerchant(String city, String merchantId) {
        Merchant m = new Merchant();
        m.setId(merchantId);
        m.setCity(city);
        return m;
    }
    
    public static Merchant createMerchantWithLocation(Double latitude, Double longitude, String merchantId) {
        Merchant m = new Merchant();
        Coordinate coordinates = new Coordinate();
        coordinates.setLatitude(latitude);
        coordinates.setLongitude(longitude);
        m.setCoordinates(coordinates);
        m.setId(merchantId);
        return m;
    }

    public static Transaction createTransaction(String date, String merchantId) {
        Transaction t = new Transaction ();
        t.setMerchantId(merchantId);
        t.setDate(dateTime(date));

        Category dummyCategory = new Category();
        dummyCategory.setId(StringUtils.generateUUID());
        dummyCategory.setType(CategoryTypes.EXPENSES);

        t.setCategory(dummyCategory);
        return t;
    }
    
    public static CityCoordinate createCityCoordinate(String name, Double lat, Double lon) {
        CityCoordinate cc = new CityCoordinate();
        cc.setCity(name);
        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(lat);
        coordinate.setLongitude(lat);
        cc.setCoordinate(coordinate);
        cc.setCountry("Sweden");
        return cc;
    }
    
    public static UserLocation createUserLocation(String date, Double lat, Double lon) {
        UserLocation location = new UserLocation();
        location.setDate(dateTime(date));
        location.setLatitude(lat);
        location.setLongitude(lon);
        return location;
    }


    public static Date date(String date) {
        try{
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date dateTime(String date) {
        try{
            return ThreadSafeDateFormat.FORMATTER_MINUTES.parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public static final Ordering<DailyCityExistence> ORDERING_EXISTENCE = new Ordering<DailyCityExistence>() {
        @Override
        public int compare(@Nullable DailyCityExistence e1, @Nullable DailyCityExistence e2) {

            int result = Longs.compare(date(e1.getDateString()).getTime(), date(e2.getDateString()).getTime());

            if (result == 0) {
                result = Ordering.natural().compare(e1.getCity(), e2.getCity());
            }

            return result;
        }
    };

    public static final Ordering<CityLocationGuess> ORDERING_CITY_GUESS = new Ordering<CityLocationGuess>() {
        @Override
        public int compare(@Nullable CityLocationGuess e1, @Nullable CityLocationGuess e2) {
            return Ordering.natural().compare(e1.getCity(), e2.getCity());
        }
    };
}
