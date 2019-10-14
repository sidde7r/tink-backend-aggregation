package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.ApiResponse;

/*
File is delimited with '|'. Escape character is '||'.
keys and values are grouped into pairs, so that value is before key (field name). E.g. |1John Doe|1Name
First character after '|' delimiter determines data type:
|I -> byte array
|0 -> null
|1 -> string
|2 -> byte
|3 -> integer
|4 -> datetime
|5 -> bool
|6 -> double
|7 -> long

Each full object ends with |D{number} - where {number} is the number of fields preceding this marker  e.g |D23 - object has 23 fields (23 keys + 23 values = 46 pipes)
|L{number} - collection marker. Items preceding this marker are stored in collection. {number} represents number of items in array.

------------
Example:
1valueA|1keyA1|1valueB|1keyB|D2|L2|1someKeyValueMap|0|L2|1anotherMap
in JSON would be

{
 anotherMap: [
  someKeyValueMap: [
    keyA:valueA,
    keyB:valueB
  ],
  null
 ]
}
 */
public class Parser {

    private static final String SPLIT_REGEX = "(?<!\\|)\\|(?!\\|)";

    ApiResponse parseResponse(String rawResponse) {
        List respArray = (List) parse(rawResponse);
        return new ApiResponse(
                (List) respArray.get(1), (String) respArray.get(0), (String) respArray.get(2));
    }

    private Object parse(String raw) {
        Stack<Object> stack = new Stack<>();

        for (String token : raw.substring(1).split(SPLIT_REGEX)) {
            if (token.startsWith("0")) {
                stack.push(null);
            } else if (token.startsWith("D")) {
                Map<Object, Object> fullObj = parseObject(stack, token);
                stack.push(fullObj);
            } else if (token.startsWith("L")) {
                List<Object> items = parseArray(stack, token);
                stack.push(items);
            } else {
                stack.push(token.substring(1).replace("||", "|"));
            }
        }
        return stack.pop();
    }

    private Map<Object, Object> parseObject(Stack<Object> stack, String token) {
        int objectFieldsCount = Integer.parseInt(token.substring(1));
        Map<Object, Object> fullObj = new HashMap<>();
        for (int i = 0; i < objectFieldsCount; i++) {
            fullObj.put(stack.pop(), stack.pop());
        }
        return fullObj;
    }

    private List<Object> parseArray(Stack<Object> stack, String token) {
        int itemsCount = Integer.parseInt(token.substring(1));
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < itemsCount; i++) {
            items.add(stack.pop());
        }
        return items;
    }
}
