package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class InitialParametersResponseTestData {

    public static InitialParametersResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InitialParametersResponse response = mapper.readValue(TEST_DATA, InitialParametersResponse.class);

        return response;
    }

    private static final String TEST_DATA = "{\n"
            + "\t\"initialParametersResponse\": {\n"
            + "\t\t\"initialParameters\": {\n"
            + "\t\t\t\"param-tabel\": {\n"
            + "\t\t\t\t\"key\": {\n"
            + "\t\t\t\t\t\"$\": \"js\"\n"
            + "\t\t\t\t},\n"
            + "\t\t\t\t\"val\": {\n"
            + "\t\t\t\t\t\"$\": \"<script type=\\\"text\\/x-nemid\\\" id=\\\"nemid_parameters\\\">\\n{\\\"LANGUAGE\\\":\\\"EN\\\",\\\"PARAMS_DIGEST\\\":\\\"ZFLh7B84CneGSOF7L3HBNT25Sw15VGkhl7gNXAPVSpQ=\\\",\\\"CLIENTMODE\\\":\\\"LIMITED\\\",\\\"REMEMBER_USERID_INITIAL_STATUS\\\":\\\"FALSE\\\",\\\"REMEMBER_USERID\\\":\\\"\\\",\\\"DIGEST_SIGNATURE\\\":\\\"h6uePac9DNkaI0qTZPOJVEycZXL+n05Rv93+8oOBW9ePgSWEV1\\/ZrspZsIg+FyYeKXyFf9WHTNqkaJTX4z0v98bs0pJmOq3L\\/zqtiiIz\\/6ueFiXfXWWXpVf6q+pJYdMCsQj21H4Pf4VlCuZg83rxvf0HXl8YJM0zkcrT6+wx3qmyojTeWVA4sPjArxQLJm7XLRJxa\\/CA5OYiiKXwCkHZKQzXeTRowDjcJi6byN+pB2Fp8RishCFNqXH9MkYmmaZGO0tWWUIrWX3vJQ4ihr1Uel8jNboKf51ztElmqeUbkYhej8TfPu\\/fAgjlA51\\/dULbu3u6njX7Ljr\\/Wy8NScougA==\\\",\\\"CLIENTFLOW\\\":\\\"BANKLOGIN1\\\",\\\"SAML_REQUEST\\\":\\\"PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNwOkF1dGhuUmVxdWVzdCB4bWxuczpzcD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIiBJRD0iREstMjAxNy0xMi0xM1QxMjozNzozMy4xOTJaLTk1NzRhIiBJc3N1ZUluc3RhbnQ9IjIwMTctMTItMTNUMTI6Mzc6MzMuMjk0WiIgVmVyc2lvbj0iMi4wIj4KPHM6SXNzdWVyIHhtbG5zOnM9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjIwOTk8L3M6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgo8ZHM6U2lnbmVkSW5mbyB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+CjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48L2RzOkNhbm9uaWNhbGl6YXRpb25NZXRob2Q+CjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ij48L2RzOlNpZ25hdHVyZU1ldGhvZD4KPGRzOlJlZmVyZW5jZSBVUkk9IiNESy0yMDE3LTEyLTEzVDEyOjM3OjMzLjE5MlotOTU3NGEiPgo8ZHM6VHJhbnNmb3Jtcz4KPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIj48L2RzOlRyYW5zZm9ybT48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIiBQcmVmaXhMaXN0PSJkcyBzIHNwIj48L2VjOkluY2x1c2l2ZU5hbWVzcGFjZXM+PC9kczpUcmFuc2Zvcm0+CjwvZHM6VHJhbnNmb3Jtcz4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiI+PC9kczpEaWdlc3RNZXRob2Q+PGRzOkRpZ2VzdFZhbHVlPjVWT3UrbVhZT2lYT0l1SjNveW1ubGpHdjJqSnRRK1l3OTFFUkFjc09QOTA9PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+CmtGcktqbFMwdmZlK0RlWGl6dzhsMmF2RFJUZGs2WXhHUFozVnpzc3M2ekhlYXM5cUhvVFRieVRJbUlQQXYzTis3OUpyeXN0OVFYUGtpeUc1MUsxdk9xc2t4bjd1VFEwaHYzaVI1dXlEU0RpejZrR3hkekpCcW5YZnNQbnNJVDJjZ1lVanN3bm90c1RVd2dWYnBDSnFlYkFDNk00OTB3Z2wvRjFKWmdNdWhLNWI3SktjRkZnWGxySmYwQUk2aWxVUWZLRDNzamd0ODFMd3k2Mk5pVWp1QmlEUEpjVE5xZXRiS0lIRWg5bGd4UzNobVIvbDZ1eFhjTFA2MnBDWEw0R3NwZHJzRXJjOHBaSDRITmEvcWp5a2NzS3BXWThZcExlcTFpcmhYcXBxT1lFZE5xSnJSNmdhZmRxMUNkMDhrZnJiallDN1ErSTZObXRBbnAybDBKamF6QT09PC9kczpTaWduYXR1cmVWYWx1ZT4KPC9kczpTaWduYXR1cmU+PHNwOkV4dGVuc2lvbnM+PGRhbmlkOlByb3RlY3RJZGVudGl0eURldGFpbHMgeG1sbnM6ZGFuaWQ9Imh0dHA6Ly9kYW5pZC5kay8yMDExLzgvUHJvdGVjdElkZW50aXR5RGV0YWlscyI+PC9kYW5pZDpQcm90ZWN0SWRlbnRpdHlEZXRhaWxzPjwvc3A6RXh0ZW5zaW9ucz48L3NwOkF1dGhuUmVxdWVzdD4=\\\"}\\n<\\/script>\\n<iframe id=\\\"nemid_iframe\\\" name=\\\"nemid_iframe\\\" src=\\\"https:\\/\\/applet.danid.dk\\/launcher\\/lmt\\/1513168653325\\\" ontouchstart=\\\"\\\" allowfullscreen=\\\"true\\\" scrolling=\\\"no\\\" frameborder=\\\"0\\\" style=\\\"width:500px;height:450px;border:0\\\">\\n<\\/iframe>\\n<form name=\\\"postBackForm\\\" action=\\\"CALLBACK-URI\\\" method=\\\"post\\\">\\n\\t<input name=\\\"response\\\" type=\\\"hidden\\\" value=\\\"\\\" >\\n<\\/form>\\n<script type=\\\"text\\/javascript\\\">\\n\\tfunction onNemIDMessage(e) {\\n\\t\\tvar event = e || event;\\n\\t\\tvar win = document.getElementById(\\\"nemid_iframe\\\").contentWindow, postMessage = {}, message;\\n\\t\\tmessage = JSON.parse(event.data);\\n\\t\\tif (message.command === \\\"SendParameters\\\") {\\n\\t\\t\\tvar htmlParameters = document.getElementById(\\\"nemid_parameters\\\").innerHTML;\\n\\t\\t\\tpostMessage.command = \\\"parameters\\\";\\n\\t\\t\\tpostMessage.content = htmlParameters;\\n\\t\\t\\twin.postMessage(JSON.stringify(postMessage), \\\"*\\\");\\n\\t\\t}\\n\\t\\tif (message.command === \\\"dont_changeResponseAndSubmit\\\") {\\n\\t\\t\\tdocument.postBackForm.response.value = message.content;\\n\\t\\t\\tdocument.postBackForm.submit();\\n\\t\\t}\\n\\t}\\n\\tif (window.addEventListener) {\\n\\t\\twindow.addEventListener(\\\"message\\\", onNemIDMessage);\\n\\t} else if (window.attachEvent) {\\n\\t\\twindow.attachEvent(\\\"onmessage\\\", onNemIDMessage);\\n\\t}\\t\\n<\\/script>\\n\"\n"
            + "\t\t\t\t}\n"
            + "\t\t\t}\n"
            + "\t\t},\n"
            + "\t\t\"sessionId\": {\n"
            + "\t\t\t\"$\": \"DK-2017-12-13T12:37:33.192Z-9574a\"\n"
            + "\t\t}\n"
            + "\t}\n"
            + "}";
}
