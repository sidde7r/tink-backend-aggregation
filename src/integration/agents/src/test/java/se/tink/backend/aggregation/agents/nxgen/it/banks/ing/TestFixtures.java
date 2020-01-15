package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.core.header.OutBoundHeaders;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

public class TestFixtures {

    public static final String BIRTH_YEAR = "2000";
    public static final String BIRTH_MONTH = "10";
    public static final String BIRTH_DAY = "01";
    private static final String BIRTH_DATE = BIRTH_YEAR + "-" + BIRTH_MONTH + "-" + BIRTH_DAY;

    public static String givenJSessionId() {
        return "PgSZZhDN1Aa6gEibmukwfG1h";
    }

    public static String givenDeviceId() {
        return "1EE79893-C2A0-22D7-A75A-1B222E22A6E3";
    }

    public static String givenPin() {
        return "132100";
    }

    public static String givenUserId() {
        return "8295222";
    }

    public static String givenBaseUrl() {
        return "https://m.ingdirect.it/";
    }

    public static String givenChallenge() {
        return "6194b44f79344dc1bc7c5599b5bcfe7d";
    }

    public static String givenOtp() {
        return "562573";
    }

    public static String givenActivationId() {
        return "862aff8d-21aa-4db3-4c321-62095f2b23d4";
    }

    public static String givenFingerprint() {
        return "AhYHcHkimuFK5b+75Tbn5+m7Mi2UXnAHd8KjJMbUFAA=";
    }

    public static String givenFpe() {
        return "rUiSwyDnY1qec+HOpjcpMQtOeY888uzh5hqUtm2pewX5Hz1dJFtOddhJ6TMPUNA8lyqG99Xqfm1pUYV2eoy6TZwOKuwY0GC1Zfk/Gr/ZRmL5fzZWmPxpkJzBxm+U1a/gu54wsGsRtcfl44PMrEcfHg5DdPjSvzcrDIZxmizS7nV/JZhSxBcUEYf4ShKXfcyNJc+kFTO3fNRqE3+6cm/HZUJdHfmGSQHKXcHk6KXJr7i/Qxl/sMNCnXXjVEoUIEdeqHfR5u1IyL3zCK95Wm9cFZaxGm4dznOeHXxqkdEfQfX7bXzTBRk9/BAys4eFBqN+iVdZMZW98Vlr1SUK8uHvqQ==|NyRlNSsDQ8yvSBuLpbcXA96zqgfkQ2TZNCJrnufWrQZRfgpgZ1fiFd9rwzQ7F54bBqGovEyDvjCreBYpORQiU5YuqRKb1/xhqGvrhi9BCozLqh8ExkqJKbJLmL4Y4eRTYvpmfwkvxDOkGvGGUi+tWgwAygT1o/4cqtIALTcWl+iN9uow1AgmbzIoLuVip1AallH2fNwFx9W+ya31TdDG4GS7Jwv79k9BvPgIkZk2OUMfBLlbfEHl8iGrQgkwo4h1ZehM5W9qfwp4KkoOrP0AvTkrt9ZGaBhxdUF8hXx2Qb7NNXkDzn7BjiqJekm2f2wcSEITZUCaaAMV3nzwMrUDyQ==|KirJnjjsZX8nYsJlosRpxNOgfWhcPeh5XnbSDQ2qt0a4J6Gj7WtrJZ0U0PIbFrS2";
    }

    public static String givenFpeUrlEncoded() {
        return "rUiSwyDnY1qec%2BHOpjcpMQtOeY888uzh5hqUtm2pewX5Hz1dJFtOddhJ6TMPUNA8lyqG99Xqfm1pUYV2eoy6TZwOKuwY0GC1Zfk%2FGr%2FZRmL5fzZWmPxpkJzBxm%2BU1a%2Fgu54wsGsRtcfl44PMrEcfHg5DdPjSvzcrDIZxmizS7nV%2FJZhSxBcUEYf4ShKXfcyNJc%2BkFTO3fNRqE3%2B6cm%2FHZUJdHfmGSQHKXcHk6KXJr7i%2FQxl%2FsMNCnXXjVEoUIEdeqHfR5u1IyL3zCK95Wm9cFZaxGm4dznOeHXxqkdEfQfX7bXzTBRk9%2FBAys4eFBqN%2BiVdZMZW98Vlr1SUK8uHvqQ%3D%3D%7CNyRlNSsDQ8yvSBuLpbcXA96zqgfkQ2TZNCJrnufWrQZRfgpgZ1fiFd9rwzQ7F54bBqGovEyDvjCreBYpORQiU5YuqRKb1%2FxhqGvrhi9BCozLqh8ExkqJKbJLmL4Y4eRTYvpmfwkvxDOkGvGGUi%2BtWgwAygT1o%2F4cqtIALTcWl%2BiN9uow1AgmbzIoLuVip1AallH2fNwFx9W%2Bya31TdDG4GS7Jwv79k9BvPgIkZk2OUMfBLlbfEHl8iGrQgkwo4h1ZehM5W9qfwp4KkoOrP0AvTkrt9ZGaBhxdUF8hXx2Qb7NNXkDzn7BjiqJekm2f2wcSEITZUCaaAMV3nzwMrUDyQ%3D%3D%7CKirJnjjsZX8nYsJlosRpxNOgfWhcPeh5XnbSDQ2qt0a4J6Gj7WtrJZ0U0PIbFrS2";
    }

    public static MultivaluedMap<String, Object> givenStaticHeaders() {
        OutBoundHeaders headers = new OutBoundHeaders();
        headers.putSingle("X-OTML-PROFILE", "appstore");
        headers.putSingle("X-OTML-NONCE", 1L);
        headers.putSingle(
                "User-Agent",
                "Mozilla/5.0 (iPhone; U; CPU OS 3_2 like Mac OS X; en-us) "
                        + "AppleWebKit/531.21.10 (KHTML, like Gecko) "
                        + "Version/4.0.4 Mobile/7B334b Safari/531.21.10");
        headers.putSingle("X-OTMLID", "1.07");
        headers.putSingle("X-OTML-ADVANCED-MANIFEST", true);
        headers.putSingle("X-APPID", "iPhone_Ing_41_3.0.15");
        headers.putSingle("X-OTML-PLATFORM", "ios");
        headers.putSingle("X-OTML-CLUSTER", "{750, 1334}");
        headers.putSingle("Accept-Language", "it-IT, it-IT;q=0.5");
        headers.putSingle("Accept", "*/*");
        headers.putSingle("Accept-Encoding", "br, gzip, deflate");
        return headers;
    }

    public static MultivaluedMap<String, Object> givenStaticHeadersUrlEncodedForm() {
        MultivaluedMap<String, Object> headers = givenStaticHeaders();
        headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    public static LocalDate givenBirthDate() {
        return LocalDate.parse(BIRTH_DATE);
    }

    public static String givenPersonId() {
        return "123456";
    }

    public static boolean notJailBroken() {
        return false;
    }

    public static Long givenTime(Clock clock) {
        return clock.millis();
    }

    public static byte[] givenAESKey() {
        return Base64.getDecoder().decode("+DLTPlc34jg30at/whjDzt4JF8OTwur1");
    }

    public static byte[] givenInitializationVector() {
        return new byte[] {1, 2, 3};
    }

    public static String givenLogin1OtmlDatasources() {

        return String.format(
                "<datasources>\n"
                        + "  <datasource key=\"response\">\n"
                        + "    <element key=\"code\" val=\"200\"/>\n"
                        + "    %s"
                        + "    <element key=\"personId\" val=\"PERSON_ID\"/>\n"
                        + "    %s"
                        + "    <element key=\"result\" val=\"ok\"/>\n"
                        + "  </datasource>\n"
                        + "</datasources>",
                givenLogin1OtmlDatasourcesImages(), givenLogin1OtmlDatasourcesPinPositions());
    }

    private static String givenLogin1OtmlDatasourcesImages() {

        List<String> images = givenKeyboardImagesBase64();
        return String.format(
                "    <element key=\"images\" val=\"\">\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "      <element key=\"\" val=\"%s\"/>\n"
                        + "    </element>\n",
                images.get(0),
                images.get(1),
                images.get(2),
                images.get(3),
                images.get(4),
                images.get(5),
                images.get(6),
                images.get(7),
                images.get(8),
                images.get(9));
    }

    private static String givenLogin1OtmlDatasourcesPinPositions() {

        List<Integer> positions = givenPinPositions();
        return String.format(
                "    <element key=\"positions\" val=\"\">\n"
                        + "      <element key=\"\" val=\"%d\"/>\n"
                        + "      <element key=\"\" val=\"%d\"/>\n"
                        + "      <element key=\"\" val=\"%d\"/>\n"
                        + "    </element>\n",
                positions.get(0), positions.get(1), positions.get(2));
    }

    public static List<String> givenKeyboardImagesBase64() {
        return ImmutableList.of(
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAABgklEQVR42mNgGAWjYBSMglEwCkbBkAf/kxik/iczzAfS68A4mcF6cDkwmWEV0GH/wTiZ4e//dAbVweQ4eaDD/iA5cONgC71euONAOIXBYTClPV4g/ogUeucGV+ilMBSghF4yQ+zgcdwqBmago+4jOe7Z/wYGtsEUeiEooZfEUDXYyr6jSI779j+bQXjwOC6NwRwt7c0YbEXLCiQH/gM6WGMwhZ4c0IG/kUJvK1Z1wCj/n8qgBsYlDNz0DL1ulOhNZXDBoe4Kkrp59CuYkxk+IIXeRTxq7yKpW06voiUPrVpLHDQOBBbCTGiWvvify8A+eByYwhCEVrTUEUgOdHZgEsNhJAd+B4ae6KBxIDCnmqKF3mwiPERHByYzLEUrWrQHjQP/FzAIAC34geTAR0CcSgR+haTnFIpcCoMeNaPXHq3VQg28jHa5lxoYWJdTu+6dCmqxkIRRuwJ30OR9B0N7kf5V3agDRx046sAR5cBkhmNIDpzAMApGwSgYBaNgFIyCIQkADPArw79NMFwAAAAASUVORK5CYII=",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAABd0lEQVR42u2XPyiFYRTGH4VEBoPBRphEMVmYKKOIrL7vu/cyMN3J4g66YrKxmGyKbFfEaDKZTEb/stwSSbxO1O1xN123HjpPnfV7fz3nPO95P8DlcrlcLtefUEhhJMTYCxH2K6oEE9UBjJG3A0LFFWOrOoARln8F0L5THcAcGkIavdaivh9VGgMGVSQHx7RmN8Y4ufdgwHVqgAVyb0MLbg7tBvVWAkyhR829FXLvTAsuh1qDui4BJpjVArQLmcJRtHA0agFGOCT3NtXc6zCwd5q/frVw5AnuXAvOLmIDuyHAjNrsTVI4Hq2a1dp7RO5tq7W381s4EgyqtXeV2nuhF44Yt+TeotrsTZF7z2EeLWrtPaZw7Ki511W2OYbVANcI7lLtWVVvYHcEmFX7X56mcLyEBbSqheOE3NvVgsugu2xzjKq9+9bJvasQUKMWjnuavyW1q2WG3Hu1VdemBnhKgAdacFk0fe7bL8Anm8UhuFwul8vl+pf6AFaOsSRnLOG6AAAAAElFTkSuQmCC",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAABp0lEQVR42u2XO0sDQRSFj69C1CKdgoW9aCFiJYqIj16xCVjsbGKSTlmEoEUKf4CFiIiNjYiFiNiIna2NWEl+gKBiIyhi4XgyIbJZiEggmyvcA4ebVPNxZubOXUClUqlUKtW/kU2jzxocWw+nddlgpbGAKUxyIVu3DS6lA57HC8gtsz6G/+wsEvEC+piRdUkUUAEVUAFjBjTIEzJNb/D3lmvcHqb4v99atDQf8Hc/EjhnC2iXClhJ+Z6ej2vcGuKiRfqK3uHC66wZOulsENC79EsE8pN1VM5ZXUWnAy+DVUCLNoduWZeqnG44yX15U7iHoxDkeyldWYAG01UppjAnC5DnLjrkyttmg7cQ4Ka0L8GOyG1ek/Y8Dkaad1Ia4FLkDI5JO39nVe9zAa1y4HwsRtLblpTchEss/NQF6Grkgnn6gd5zzfcEbTX6Xi9hDuiv2IaF0kzHRZ4j2/XKesd6wXrIes36VGPkChr9nvbQt3XMgjfsg+PxnCmO8KXB06VVK6ky1IdL1sdyU8b+H+AsEkxnhDAL7rvEw6zNYEBUG1GpVCqVSr6+AVKhZ3NdZ6LIAAAAAElFTkSuQmCC",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAACAUlEQVR42u2YTShEURTHj49IplAKYUGSBTtloWTHjhoLxkZzZyZEDc2CbJTsFErKwk6SBSVlxY6ysyALkZVJk/KxwPi4/u+ZdOeZyQyeOer+6/Rmppnur3vPued/hkhLS0tLS0vrX0i6aRix9u0QtCL7qNI+QEH7WEj+KAS127mD278AWGUf4CBlSw8VJRw+KgHUrgJ3yCtnPTSmwIUB3MAJrhlgzwpggA9cPxUDKqjAbUhJaTzgVikDQDsK3DmulgI+uydogm/eeakFYK8fgB7ycyqKMuxYSNm9dT5w45Rpue/OpJ/yOfXoKaVbPCLqORVFa1Qr89AIN4dzYOm3RpEcAXwRTy+quDDVgDdfmIMQwpVKQCcABkyvKGjG7BpuOo0BuikDlMvj2NHWkIsd5lFHg87yyk+j7akeUtALopEX5GfjsMdvlhE0qhzzvbGz3ACj70of1XA8ZrVYnLwAe6nUcuU0cZtN2ixHnMcNcFJ1OrzgvFQNqDvFTCzZWY21WKAiieJwRJkJQdf4rNyuSnRggSvTrbwPRd1yiHLiGNh0fMeNuLBUb4+du+eKYQDCiBPEFt7P4Tkfgb+M4WyW7c0ljI5YZAEAt0n+HxPEbzr/LulhmZCHwpxBBD3E8YBPeB4jplN6pURslTHNNRn5hdddAKpDDmaRlpaWlpaWVqJ6A5j3Th5Dv/jyAAAAAElFTkSuQmCC",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAC2klEQVR42u1YTUhVQRQ+Zb8WFESLKAKpqGgREhRWCgWt+tkHFXXvMx+F5tOKlBa1EtGtiwgqKFJcBNGqoiJEKjdthCICpcBCo4Toh6ymb+4znXuauvOeM9cWc+Bw37t35p5vzsx35ptL5M2bN2/evHkzNXGclohzNOf/AZShTSKkKyKgPvgHuMD/H7gO4HoHz+sAeF76wGpoIwDcjAAleUhD8BNC0Ix0wIVUDv+kAfIW/gg+iP8/NWBb3IM7SssA4LUC6iuu7aKWlrJ2pXiWg79nIA+7BRjQXQXcGIBsT1ijZRNrM+9fRD0tdkWIMjalZw2XxEHWL3AF8FAsUIbWG/VDxtg0t7siR5MS5HMhrET7V0oGL7oCmItlAoQx6oc6iL7flMw3ugK4l03xAcPs7WD9drkqMaUIMKwEeyOytPyffXI0HwPrV6b3JUrSXJdb2xnGyKeS3dq2x2gh2txgBNnntg52UwlAdTGQH3HNYq3NUrK9BfeesXat6Wx1AIKAlzTb2QC8AX6L3f8uM5++YAioAll5kiAUHopq2jA9UquatgJEb4KaeQc/JcmSHrA8Mzs12bofgQnpuebZoJRoaaiZRQjYwwD0SlJMtMEOI9kKf/EHmVBL3TI4oMcsaJvKXk2m28YV9qSaqaE1rkiRZeCaDGtnPev3wLq6jqY2pBElUA8yN7OAbfIy2+522mbsHhagssABrmNZPG17eptjhRf7chFiY0QB2GUb4HXl5f1FvuOeWsBti4Rr6umtSLk2pLyj07YWbI2toQSZpVmDK1ntbLYNcPdUFjn6XGD9V9suM7MjgToZQEr4CsPBZRg4Z2eSqtjZIqTRiN0nacFfBrVqfM9WZVmfW0XNd5PfiiWk2/AO+Hn41QiIOpi8D4NsK9L4olXHziZJH4/GojVoeAq0AxLTKokSW5f6zHbjunb6P1qGtBmZ3Q8/gt/b5D3/OdebN2/evHkzsl+u8jSnE9FcQQAAAABJRU5ErkJggg==",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAB1ElEQVR42u2YTShEURTH/yGf+QoLLMQaCxZ2kiwsJ9Y27z0iTfnciUlZWs6IYq2ZZCNZGClZWJiFkpTsZCHZmBLpOm+Geu+axpPezFHnX6dT7y3u7/3vuefdewGRSCQSiURepCZRp0Io4gEzhkZlYEaZ2KR8SvmRsqL8SvmK8i5FIPdgJlooIhQvKaCf44Q+pic3cBZWCOwtC8xzxuf2x1jo9h/QwIFj0CTldRp4iBzqoCj/nPp6etZP7+Ia5K2aQo3f09tOkaAB59UEaj04PqtBbvBb2Sb2HIA3HAEDLhc9OJ9bQAutLsBRDPACnEaZVoeD3Ka4V2s7TdwAFx3uPfCCs3+HX7/AdGxxbjFJNY5mTqs3pNXeMifnFrSVe0TbsGIecAaWNOcuqBar8w8WQgE5Fdacu2ZRd/b0EVBMc+5cBdHAAa6UYPa/1ZyByvzDzaGCYA4152LkXEn+4aIodG1g085F7Frk0kpWXXAW1jj1uRHNuSgf54Kocv1fTZyxacIZdifv1IS7+MDRqYzAnhyAYV67EwPD2vmije/m08A9xxPatmN6dzgCJhyAd6lLo9/HsX2/41cNXnq8MMoeFvr8Aoz/Gc6+nrPQCZFIJBKJRP9GH3cBH0EPXPAiAAAAAElFTkSuQmCC",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAACnklEQVR42u1YzUtVQRT/VRYRIRQUhWAIbaygqE1FVIsMXLUpgxZp7973LEEMatEiQWoblbUKl9KiRxH+B30QWVhiPGz5FkZRYVloRR9Ov5nX07mj8j5qplnMgcNc7p1zz2/OOXPOmQECBQoUKFCgQNWQOIVVohO1/gCKsV9EuEkeEil8JAvFET6Tc3y+xDk7hcAit8DSaKbypzOASnOvS6udocLpOSAi/OCY5/ia40/jW5cbcNJlSWBfyTdo0W0iiyUz8/hMUFu4mHMcB8RJrHVhucMGuLxox1Y/NgMtQEDvNHCvRAfW+bNbI/Rp8fSL4y6fwK0moC8awAG/km+Es4nYS2OfXwBTeKFZb8gvcJ1Yk8h5EWK/AKbRknBvOzb55t4rGsBJ0YPF6j1rK8FvZm48SqteJGfJl8mtKmn3YJkrgLc0gA9FBks5thHIaIna+4lzjrkAeE+Lv1HyWAUNgpTpt9p+UcnLBZTnVQ2W1kzhIN26m+4+oUIiwoQB8q7NHPjBUDZCPlKMxXllMqjnnPsJuQz2/HtwhXibVRKju9zGU7qVIN9r8oM2GoQ6o4K0VGj9LsP6G23UYN2CmQo9sN5YYLONGPymu7gK+SmrXfWf3VpUcL0KgDltgddsAHykAcz+FUBWHBsuvq0n6irkx7U4bLNxDuk28tn2smU7sNLYxXvtpJrCcbKo5GoF1mtNAOS/bFWTO5qit/J6o0y5B9rCctZuF5i/Dhh1+FkpkHOSdIxD9uqx7P0iDBvx9FwdzA2rMEZX8P15Y+5jFwenDVT2Zp52alzdHDBHkp+QvxvfJ+Tlkas7mUZlufL7wUHKNLg9AsgOJ8bpBYHKHV9IzhfYktX877vBBnIT+bhqXGPsIKjlCBQoUKBAgQKVot9xB9F2ms19aQAAAABJRU5ErkJggg==",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAACj0lEQVR42mNgGAWjYBSMglEwCkYBseB/GoPI/ywGnsHjoGQG+f9JDPVAfBWIvwPxfzBOZvgExDuB7IT/uQzs9HfYfwZGoAPKgfg33FG4cDLDlf8pDMb0c9wqBmagpeuxOOQDkL4EpJ9hceg3oCMt6OPAJIYeNMtPAC13QlGTzqAKdOgiNA+8BqZPWdo6LpXBHc1x6/ClMaDDK9AcuYjWmeI0kmV3/pcwcBOhZxOSnr/AnK5Lq9DzQQsNbyL1aaPp66VV2luGZNEtUE4mQe91JAfeo0UBzAo0/D3ckhSGWhKTRgtKKKYzaFHXgSkMDigWpDJYkZg8DNCiOYLamSMfyYLvoBAlI4l8QzKjidoOnIFcM5CZhh8hOXA1tR14CMmBm8h04FkkB+6ltgNvIzlwKpkO3IFkxklqO/AFkuGdZGa0JUhmXKN2GfgFKXrqyTRjIk3KQnDTKonhH1IZWEFmLEyjNKPh8/0fpBBsJ9OBC5BbQNR24Eck388m04GrkBy4m9oOfILcxCLTjKNInlxBbQdeRTL8EJkh+AwpHTdQu5jZQEkRAerpoWQ0GtTFbSh1MRENVbQYcENpLFC70Qp0YBRaUz+YbA+CopqEtiSxtYA4uLmOsGQxiV1U5KpyFq1a1IeRLHn3v4GBhazoTWbwoI0DUxgy0SxKIqolnsxwHknPeapHL9yyBgY2oCX3kRz5/n8mgxKBtNeN4qkUBn9adzsj0ELxGTCUbDDUFTAIAOVnoqldTq8Bo24sQxtHgeITQE15aO/vI5rjzgM9wkW/gaMUhi6UXI1/8Ggt0HH8AzH0ZgdtJf/B4bjDQI9EDvwYIaiMBBUlKQyJQJwMxK6gcUOGUTAKRsEoGAWjYBQQAwCLecmDRDeOKQAAAABJRU5ErkJggg==",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAClklEQVR42u2YT4hNURzHv2OBhAgj/xULSUksLIhSlrKwERbuvXON0RQL2ZjSULNi4V+NhWxEGmHBFGEa8mdhJ81obEzKsxLSZHB8731v8rvHm9u7T783Z3F+9eu87jun3+d8zzm/87sX8ObNmzdv3rzVYqYdU+jzzAlMcgOoFctNgC4T4g7bt/SfdFNp3/P5Jfq2iVEqQgchvleA8j3EbRNjQWPg2rCEAYeqQPxi+47t8DiQA4Scqw8Y4r4V+DkDb+K+myqWfhEV3pUCZ/s+0oUL0GoFPG4MmsbtH2E++72y1NyhtbTTCfRVBOrLgxOTmkEviXEvdAAjbLTUW1NA+S4x9jd9oQZgLOC+FE5H5UM0Btmisf/OC8CXdRyuZ2L8OY3T2ysC3Khj/DWh4AMNBS+LAE/qADwjJjiksQc7RIDhOiZ4VEywpKHgHiufbSio4D4x9pvWFTciVOyteWyMpex/V4wd1brmTlu58Lo5iNk522I9+3VXKSo+6QAewhxCfbaClfish20n27YkhdD7q/STE3ujdx8nN0qIwZrKrL9APdYS9+sWDTGmEfSsdTvYUKP0W+y3uXLIHor/bjamNmSJZQ5gLSF2M+hJ+sW0wgmxk0XtTCsLyNKr2613Fb6jEPqHUPCUW4Dldxe5/JFbgAHaM4AxVrkFGOKp6jX3n2lpcaVIHVveq66pdySzvC3Y4g7cYcwi4Aeh3mvX1LuSUS/CXpfy3gX764J+YFYuaV0XYlnOodjK/x9bcB+ZC5sbsWTHMp8ykm8uiVJJOV/+PVDlTh6kr2xUwu0rVMUEuJdXL2oouC59/ZQn81/FRtLDwb4TdwgMmljFrCbQdu65/YQJku+AvMJW8IBMhjdv3rx58+atiP0BIknE3ok8b5IAAAAASUVORK5CYII=",
                "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAA/UlEQVR42u2YPQrCQBCFnyIIouANPIJnEMHOwitk/QFBsLH3DhaCYC8IaTyD4CmCWKw2egAL1zEEiSEpROJGeR88ttjAfMwOCxuAEEIIIX+BGaJsHEyMwkpWV9Z2duQctCQHiXlGYZYFsYqILF7EsiIoAk3JPlbOpqA/a13MY6TWInWxKiiFG1LYi3RKmx46QVe1NUEzQF0K3yJySzNGNXTsFgWnKEjhXVDce8xgzFxqu0esUPPvuj5KCfs6U9cMBSlIQQpSkIIUpCAFKUhBCn5VwCAnxTeSbWwUrqFn6THhOzc9wRGKib863kiab+O8dOn8kaDCCYQQQgj5Ge6pY9Iyj7oxvAAAAABJRU5ErkJggg==");
    }

    public static Map<Integer, Integer> givenMapOfKeyboardImageValueToIndex() {
        return ImmutableMap.<Integer, Integer>builder()
                .put(4, 0)
                .put(7, 1)
                .put(5, 2)
                .put(3, 3)
                .put(8, 4)
                .put(2, 5)
                .put(6, 6)
                .put(0, 7)
                .put(9, 8)
                .put(1, 9)
                .build();
    }

    public static List<Integer> givenPinPositions() {
        return ImmutableList.of(2, 4, 5);
    }
}
