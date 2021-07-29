package se.tink.backend.aggregation.agents.framework.wiremock.utils.parsingrules.parsingoperations;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.List;

public class BeautifyJsonString implements ParsingOperation {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public List<String> performOperation(String line) {
        try {
            JsonElement je = JsonParser.parseString(line);
            return ImmutableList.of(gson.toJson(je));
        } catch (JsonSyntaxException jsx) {
            return ImmutableList.of(line);
        }
    }
}
