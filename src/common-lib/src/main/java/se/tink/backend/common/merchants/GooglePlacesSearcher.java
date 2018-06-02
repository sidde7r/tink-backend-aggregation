package se.tink.backend.common.merchants;

import com.google.common.base.Charsets;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.restfb.json.JsonException;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.CityCoordinate;
import se.tink.backend.core.Coordinate;
import se.tink.backend.core.Location;
import se.tink.backend.core.Place;
import se.tink.backend.core.StringStringPair;
import se.tink.backend.utils.LogUtils;

public class GooglePlacesSearcher {

    private static final String PLACES_API_KEY = "AIzaSyBbD-9t5m3EnL9U8vxD-3qrg9OKUrVZ92g";
    private static final String PLACES_TEXTSEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final String PLACES_NEARBYSEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String PLACES_DETAILS_REQ = "https://maps.googleapis.com/maps/api/place/details/json?";
    private static final String PLACES_COORDIATES_REQ = "https://maps.googleapis.com/maps/api/geocode/json?";
    private static final String PLACES_PHOTO_REQ = "https://maps.googleapis.com/maps/api/place/photo";

    private final String apiKey;
    private DefaultHttpClient placesClient;
    private DefaultHttpClient placesProxyClient;

    private enum PlaceType {
        ALL,
        ESTABLISHMENT,
        ADDRESS
    }

    private static final LogUtils log = new LogUtils(GooglePlacesSearcher.class);

    public GooglePlacesSearcher() {
        this(PLACES_API_KEY);
    }

    public GooglePlacesSearcher(final String apiKey) {
        this.apiKey = apiKey;

        HttpHost proxy = new HttpHost("54.217.221.65", 3128);
        placesProxyClient = new DefaultHttpClient();
        placesProxyClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        placesClient = new DefaultHttpClient();
    }

    private StringBuilder createBasicRequest(String api, String locale) {
        StringBuilder urlBuilder = new StringBuilder(api);
        urlBuilder.append("&key=").append(apiKey).append("&language=")
                .append(Catalog.getLocale(locale).getLanguage());
        return urlBuilder;
    }

    public void setHttpParam(String param, Object value) {
        placesClient.getParams().setParameter(param, value);
        placesProxyClient.getParams().setParameter(param, value);
    }

    /**
     * Uses the geocode API to convert an address to coordinates.
     *
     * @param address
     * @return
     * @throws Exception
     */
    private JSONArray getGeocodeResult(String address, String locale) throws Exception {
        StringBuilder urlBuilder = createBasicRequest(PLACES_COORDIATES_REQ, locale);
        urlBuilder.append("&address=").append(URLEncoder.encode(address, "utf8"));

        log.debug("Geocode search for " + address);

        HttpGet request = new HttpGet(urlBuilder.toString());
        HttpResponse response = placesClient.execute(request);

        JSONArray results = getResults(response);

        log.debug(String.format("\tresults: %d", results.length()));

        return results;
    }

    private JSONObject getFirstGeocodeResult(String address, String locale) throws Exception {
        JSONArray results = getGeocodeResult(address, locale);

        return results.getJSONObject(0);
    }

    public Optional<Coordinate> getCoordinate(String address, String locale) throws Exception {
        JSONArray results = getGeocodeResult(address, locale);

        if (results.length() == 0) {
            return Optional.empty();
        }

        return Optional.of(getCoordinates(results.getJSONObject(0)));
    }

    public Optional<Coordinate> getGeocodeResultIfOne(String address, String locale) throws Exception {
        JSONArray results = getGeocodeResult(address, locale);

        if (results.length() != 1) {
            return Optional.empty();
        }
        return Optional.of(getCoordinates(results.getJSONObject(0)));
    }

    public Coordinate geocode(String address, String locale) throws Exception {
        JSONObject result = getFirstGeocodeResult(address, locale);
        return getCoordinates(result);
    }

    public CityCoordinate geocodeCity(String address, String locale) throws Exception {
        JSONObject result = getFirstGeocodeResult(address, locale);
        Location location = getLocation(result);

        return new CityCoordinate(address, location.getCountry(), location.getCoordinate());
    }

    /**
     * Uses the place/nearbysearch API find merchants nearby the submitted coordinate
     *
     * @param query
     * @param latitude
     * @param longitude
     * @return
     * @throws Exception
     */
    public List<Place> nearbySearch(String query, double latitude, double longitude, String locale) throws Exception {
        List<Place> places = Lists.newArrayList();

        StringBuilder urlBuilder = createBasicRequest(PLACES_NEARBYSEARCH_URL, locale);
        urlBuilder.append("&location=").append(latitude).append(",").append(longitude);
        urlBuilder.append("&rankby=distance");
        urlBuilder.append("&keyword=").append(URLEncoder.encode(query, "utf8"));
        // urlBuilder.append("&name=" + URLEncoder.encode(query, "utf8"));

        log.debug("Nearbysearch for " + query);

        HttpGet request = new HttpGet(urlBuilder.toString());
        HttpResponse response = placesClient.execute(request);

        JSONArray results = getResults(response);

        log.debug(String.format("\tresults: %d", results.length()));

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            places.add(getPlace(result));
        }
        return places;
    }

    /**
     * Uses the place/textsearch API to lookup places.
     *
     * @param query
     * @return
     * @throws Exception
     */
    public List<Place> textSearch(String query, String locale, String country) throws Exception {
        List<Place> places = Lists.newArrayList();

        StringBuilder urlBuilder = createBasicRequest(PLACES_TEXTSEARCH_URL, locale);

        urlBuilder.append("&query=");

        switch (country) {
        case "SE":
            urlBuilder.append(URLEncoder.encode(query + " in Sweden", Charsets.UTF_8.name()));
            break;
        case "NL":
            urlBuilder.append(URLEncoder.encode(query + " in Netherlands", Charsets.UTF_8.name()));
            break;
        case "US":
            urlBuilder.append(URLEncoder.encode(query + " in USA", Charsets.UTF_8.name()));
            break;
        case "UK":
            urlBuilder.append(URLEncoder.encode(query + " in UK", Charsets.UTF_8.name()));
            break;
        case "FR":
            urlBuilder.append(URLEncoder.encode(query + " in France", Charsets.UTF_8.name()));
            break;
        default:
            log.warn("Unknown country: " + country);
            urlBuilder.append(URLEncoder.encode(query, Charsets.UTF_8.name()));
            break;
        }

        log.debug("Text search for " + query);

        HttpGet request = new HttpGet(urlBuilder.toString());
        HttpResponse response = placesClient.execute(request);

        JSONArray results = getResults(response);

        log.debug(String.format("\tresults: %d", results.length()));

        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            places.add(getPlace(result));
        }

        return places;
    }

    /**
     * Uses place/autocomplete API to auto complete a place search. Returns list of places with placeId and name only.
     *
     * @param query
     * @param maxNumberOfResults
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    public List<StringStringPair> autocomplete(String query, int maxNumberOfResults, double latitude, double longitude,
            long radius, String locale, String country) throws IOException, ParseException, JSONException {
        return autocomplete(query, maxNumberOfResults, latitude, longitude, radius, PlaceType.ALL, locale, country);
    }

    /**
     * Uses place/autocomplete API to auto complete a place search. Returns list of places with placeId and name only.
     *
     * @param query
     * @param maxNumberOfResults
     * @param latitude
     * @param longitude
     * @param radius
     * @param placeType
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    public List<StringStringPair> autocomplete(String query, int maxNumberOfResults, double latitude, double longitude,
            long radius, PlaceType placeType, String locale, String country) throws IOException, ParseException {

        List<StringStringPair> autoCompletes = Lists.newArrayList();

        StringBuilder urlBuilder = createBasicRequest(PLACES_AUTOCOMPLETE_URL, locale);
        urlBuilder.append("&input=").append(URLEncoder.encode(query, "utf8"));
        urlBuilder.append("&components=country:").append(country);

        // no location bias
        urlBuilder.append("&location=").append(latitude).append(",").append(longitude);
        urlBuilder.append("&radius=").append(radius);

        // Filter out the specified type
        if (placeType == PlaceType.ESTABLISHMENT) {
            urlBuilder.append("&types=establishment");
        } else if (placeType == PlaceType.ADDRESS) {
            urlBuilder.append("&types=address");
        }

        HttpGet request = new HttpGet(urlBuilder.toString());

        HttpResponse response = placesClient.execute(request);

        try {
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
            JSONArray results = jsonObject.getJSONArray("predictions");

            log.debug(String.format("Auto complete Query: [%s] Results [%d]", query, results.length()));

            int limit = maxNumberOfResults == 0 ? results.length() : Math.min(results.length(), maxNumberOfResults);

            for (int i = 0; i < limit; i++) {
                StringStringPair pair = new StringStringPair(results.getJSONObject(i).getString("place_id"), results
                        .getJSONObject(i).getString("description"));

                autoCompletes.add(pair);
            }
        } catch (Exception e) {
            log.error(String.format("Could not parse response from google for query: [%s]", query), e);
            EntityUtils.consume(response.getEntity());
        }

        return autoCompletes;
    }

    /**
     *
     * @param query
     * @param maxNumberOfResults
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public List<StringStringPair> autocompleteAddress(String query, int maxNumberOfResults, String locale,
            String country)
            throws IOException, JSONException {
        return autocomplete(query, maxNumberOfResults, 0, 0, 20000000, PlaceType.ADDRESS, locale, country);
    }

    public List<StringStringPair> autocompleteEstablishment(String query, int maxNumberOfResults, String locale,
            String country) throws Exception {
        return autocompleteEstablishment(query, maxNumberOfResults, 0, 0, 20000000, locale, country);
    }

    public List<StringStringPair> autocompleteEstablishment(String query, int maxNumberOfResults, double latitude,
            double longitude, long radius, String locale, String country) throws Exception {
        return autocomplete(query, maxNumberOfResults, latitude, longitude, radius, PlaceType.ESTABLISHMENT, locale,
                country);
    }

    /**
     * @param query
     * @param maxNumberOfResults
     * @return
     * @throws Exception
     */
    public List<Place> detailedAutocompleteEstablishment(String query, int maxNumberOfResults, String locale,
            String country) throws Exception {
        return detailedAutocompleteEstablishment(query, maxNumberOfResults, 0, 0, 20000000, locale, country);
    }

    /**
     *
     * @param query
     * @param maxNumberOfResults
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     * @throws Exception
     */
    public List<Place> detailedAutocompleteEstablishment(String query, int maxNumberOfResults, double latitude,
            double longitude, long radius, String locale, String country) throws Exception {
        return detailedAutocomplete(query, maxNumberOfResults, latitude, longitude, radius, PlaceType.ESTABLISHMENT,
                locale, country);
    }

    /**
     * Uses place/details API to get details information based on placeId.
     *
     * @param placeId
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    public Place details(String placeId, String locale) throws IOException, ParseException, JSONException {
        StringBuilder urlBuilder = createBasicRequest(PLACES_DETAILS_REQ, locale);
        urlBuilder.append("&placeid=").append(placeId);

        HttpGet request = new HttpGet(urlBuilder.toString());
        HttpResponse response = placesClient.execute(request);

        JSONObject result = getResult(response);

        log.debug("Details search on placeId: " + placeId);

        return getDetailedPlace(result);
    }

    /**
     * Uses place/details API to get details information based on reference (old reference, to be deprecated by Google).
     *
     * @param reference
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws JSONException
     */
    public Place detailsoOnReference(String reference, String locale) throws IOException, ParseException,
            JSONException {
        StringBuilder urlBuilder = createBasicRequest(PLACES_DETAILS_REQ, locale);
        urlBuilder.append("&reference=").append(reference);

        HttpGet request = new HttpGet(urlBuilder.toString());
        HttpResponse response = placesClient.execute(request);

        JSONObject result = getResult(response);

        log.debug("Details search on placeId: " + reference);

        return getDetailedPlace(result);
    }

    /**
     * Uses the place/photo API to get photos associated to the placeId.
     *
     * @param reference
     * @return
     */
    public File photo(String reference, String locale) {
        StringBuilder urlBuilder = createBasicRequest(PLACES_PHOTO_REQ, locale);
        urlBuilder.append("&maxwidth=400");
        urlBuilder.append("&photoreference=").append(reference);

        log.debug("Photo for " + reference);

        HttpGet request = new HttpGet(urlBuilder.toString());

        File file = null;
        try {
            file = File.createTempFile("photo-", ".jpg");

            HttpResponse response = placesClient.execute(request);
            byte[] rsp = EntityUtils.toByteArray(response.getEntity());

            Files.write(rsp, file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Get a list of detailed merchants, being the autocomplete matches to a query. With location biasing.
     *
     * @param query
     * @param maxNumberOfResults
     * @param latitude
     * @param longitude
     * @param radius
     * @param placeType
     * @return
     * @throws Exception
     */
    public List<Place> detailedAutocomplete(String query, int maxNumberOfResults, double latitude, double longitude,
            long radius, PlaceType placeType, String locale, String country) throws Exception {
        List<Place> places = Lists.newArrayList();
        List<StringStringPair> placeReferences = autocomplete(query, maxNumberOfResults, latitude, longitude, radius,
                placeType, locale, country);

        for (StringStringPair pair : placeReferences) {
            Place result = details(pair.getKey(), locale);

            if (result != null) {
                places.add(result);
            }
        }

        return places;
    }

    /**
     * With location biasing. All place types.
     *
     * @param query
     * @param maxNumberOfResults
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     * @throws Exception
     */
    public List<Place> detailedAutocomplete(String query, int maxNumberOfResults, double latitude, double longitude,
            long radius, String locale, String country) throws Exception {
        return detailedAutocomplete(query, maxNumberOfResults, latitude, longitude, radius, PlaceType.ALL, locale,
                country);
    }

    /**
     * Use no location biasing.
     *
     * @param query
     * @param maxNumberOfResults
     * @param placeType
     * @return
     * @throws Exception
     */
    public List<Place> detailedAutocomplete(String query, int maxNumberOfResults, PlaceType placeType, String locale,
            String country) throws Exception {
        return detailedAutocomplete(query, maxNumberOfResults, 0, 0, 20000000, placeType, locale, country);
    }

    /**
     * No location biasing. All place types.
     *
     * @param query
     * @param maxNumberOfResults
     * @return
     * @throws Exception
     */
    public List<Place> detailedAutocomplete(String query, int maxNumberOfResults, String locale, String country)
            throws Exception {
        return detailedAutocomplete(query, maxNumberOfResults, PlaceType.ALL, locale, country);
    }

    /* JSON helpers */

    private JSONObject getResult(HttpResponse response) throws JSONException, ParseException, IOException {
        return getJSONObject(response, "result");
    }

    private JSONObject getJSONObject(HttpResponse response, String root)
            throws JSONException, ParseException, IOException {
        JSONObject jsonObject = validateResponse(response);

        if (jsonObject == null) {
            return null;
        }

        return jsonObject.getJSONObject(root);
    }

    private JSONArray getResults(HttpResponse response) throws JSONException, ParseException, IOException {
        JSONObject jsonObject = validateResponse(response);

        return jsonObject.getJSONArray("results");
    }

    private JSONObject validateResponse(HttpResponse response) throws JSONException, IOException {
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new JsonException("Http error: " + response.getStatusLine());
        }

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
        String status = (String) jsonObject.get("status");

        if (status.equals("ZERO_RESULTS")) {
            return jsonObject;
        }

        if (!status.equals("OK")) {
            log.debug("\tstatus: " + status);
            return null;
        }
        return jsonObject;
    }

    /**
     * Get a place (with basic information) from result object.
     *
     * @param result
     * @return
     * @throws JSONException
     */
    private Place getPlace(JSONObject result) throws JSONException {
        Place place = new Place();
        place.setPlaceId(result.getString("place_id"));
        place.setName(result.getString("name"));
        if (result.has("formatted_address")) {
            place.setAddress(result.getString("formatted_address"));
        } else {
            place.setAddress(result.getString("vicinity"));
        }
        place.setTypes(getTypes(result));
        return place;
    }

    /**
     * Get a place (with detailed information) from result object.
     *
     * @param result
     * @return
     * @throws JSONException
     */
    private Place getDetailedPlace(JSONObject result) throws JSONException {
        if (result == null) {
            return null;
        }

        Place place = getPlace(result);

        place.setPostalCode(getPostalCode(result));
        place.setPhotoReference(getPhotoReference(result));
        place.setPhotoAttributions(getPhotoAttributions(result));
        place.setLocation(getLocation(result));

        try {
            place.setWebsite(result.getString("website"));
        } catch (JSONException e) {
        }

        try {
            place.setPhoneNumber(result.getString("formatted_phone_number"));
        } catch (JSONException e) {
        }

        return place;
    }

    /**
     * Extract photo reference from result object.
     *
     * @param result
     * @return
     */
    private String getPhotoReference(JSONObject result) {
        try {
            JSONArray photos = result.getJSONArray("photos");

            if (photos != null && photos.length() > 0) {
                return photos.getJSONObject(0).getString("photo_reference");
            }
        } catch (JSONException e) {
        }

        return null;
    }

    /**
     * Extract photo attribution from result object.
     *
     * @param result
     * @return
     */
    private String getPhotoAttributions(JSONObject result) {
        String attributionsString = null;

        try {
            JSONArray photos = result.getJSONArray("photos");

            List<String> attributions = Lists.newArrayList();

            if (photos != null && photos.length() > 0) {
                JSONArray htmlAttributions = photos.getJSONObject(0).getJSONArray("html_attributions");
                for (int j = 0; j < htmlAttributions.length(); j++) {
                    attributions.add(htmlAttributions.getString(j));
                }
            }

            attributionsString = StringUtils.join(attributions, ". ");
        } catch (JSONException e) {
        }

        return attributionsString;
    }

    /**
     * Extract coordinates from result object.
     *
     * @param result
     * @return
     */
    private Coordinate getCoordinates(JSONObject result) {
        Coordinate coordinates = new Coordinate();

        try {
            JSONObject geometry = result.getJSONObject("geometry");

            if (geometry != null) {
                JSONObject location = geometry.getJSONObject("location");

                if (location != null) {

                    coordinates.setLatitude(location.getDouble("lat"));
                    coordinates.setLongitude(location.getDouble("lng"));
                }
            }
        } catch (JSONException e) {
        }

        return coordinates;
    }

    /**
     * Extract location from result object.
     *
     * @param result
     * @return
     */
    private Location getLocation(JSONObject result) {
        Location location = new Location();
        location.setCoordinate(getCoordinates(result));

        try {
            JSONArray components = result.getJSONArray("address_components");

            if (components != null && components.length() > 0) {
                String street = null;
                String streetNumber = null;
                for (int i = 0; i < components.length(); i++) {
                    JSONObject component = components.getJSONObject(i);
                    JSONArray types = component.getJSONArray("types");
                    for (int j = 0; j < types.length(); j++) {
                        if (types.get(j).equals("locality")) {
                            location.setCity(component.getString("long_name"));
                        } else if (types.get(j).equals("postal_town")) {
                            // potal_town is less detailed then locality, use locality if present.
                            if (Strings.isNullOrEmpty(location.getCity())) {
                                location.setCity(component.getString("long_name"));
                            }
                        } else if (types.get(j).equals("country")) {
                            location.setCountry(component.getString("long_name"));
                        } else if (types.get(j).equals("route")) {
                            street = component.getString("long_name");
                        } else if (types.get(j).equals("street_number")) {
                            streetNumber = component.getString("long_name");
                        }
                    }
                }
                if (!Strings.isNullOrEmpty(street) && !Strings.isNullOrEmpty(streetNumber)) {
                    location.setAddress(street + " " + streetNumber);
                }
            }
        } catch (JSONException e) {
        }

        return location;
    }

    /**
     * Extract postal code from result object.
     *
     * @param result
     * @return
     */
    private String getPostalCode(JSONObject result) {
        try {
            JSONArray components = result.getJSONArray("address_components");
            if (components != null && components.length() > 0) {
                for (int i = 0; i < components.length(); i++) {
                    JSONObject component = components.getJSONObject(i);
                    JSONArray types = component.getJSONArray("types");
                    for (int j = 0; j < types.length(); j++) {
                        if (types.get(j).equals("postal_code")) {
                            return component.getString("long_name");
                        }
                    }
                }
            }
        } catch (JSONException e) {
        }

        return null;
    }

    /**
     * Extract list of types from result object.
     *
     * @param result
     * @return
     */
    private List<String> getTypes(JSONObject result) {
        List<String> types = Lists.newArrayList();

        try {
            JSONArray typesJsonArray = result.getJSONArray("types");
            for (int j = 0; j < typesJsonArray.length(); j++) {
                types.add(typesJsonArray.getString(j));
            }
        } catch (JSONException e) {
        }

        return types;
    }

}
