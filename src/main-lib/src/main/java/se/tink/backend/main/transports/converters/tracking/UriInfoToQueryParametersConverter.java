package se.tink.backend.main.transports.converters.tracking;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public class UriInfoToQueryParametersConverter {
    public static Map<String, List<String>> getQueryParameters(UriInfo uri) {
        MultivaluedMap<String, String> queryParameters = uri.getQueryParameters();
        Map<String, List<String>> map = Maps.newHashMap();
        queryParameters.forEach((key,mapOfParameters)-> {
            List<String> parameters = Lists.newArrayList();
            mapOfParameters.forEach(parameter -> parameters.add(parameter));
            map.put(key, parameters);
        });
        return map;
    }
}
