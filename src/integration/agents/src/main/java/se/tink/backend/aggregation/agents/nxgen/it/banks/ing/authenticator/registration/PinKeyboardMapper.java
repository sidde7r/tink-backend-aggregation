package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PinKeyboardMapper {

    private static final Map<String, Integer> IMAGE_TO_VALUE_MAP =
            ImmutableMap.<String, Integer>builder()
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAACj0lEQVR42mNgGAWjYBSMglEwCkYBseB/GoPI/ywGnsHjoGQG+f9JDPVAfBWIvwPxfzBOZvgExDuB7IT/uQzs9HfYfwZGoAPKgfg33FG4cDLDlf8pDMb0c9wqBmagpeuxOOQDkL4EpJ9hceg3oCMt6OPAJIYeNMtPAC13QlGTzqAKdOgiNA+8BqZPWdo6LpXBHc1x6/ClMaDDK9AcuYjWmeI0kmV3/pcwcBOhZxOSnr/AnK5Lq9DzQQsNbyL1aaPp66VV2luGZNEtUE4mQe91JAfeo0UBzAo0/D3ckhSGWhKTRgtKKKYzaFHXgSkMDigWpDJYkZg8DNCiOYLamSMfyYLvoBAlI4l8QzKjidoOnIFcM5CZhh8hOXA1tR14CMmBm8h04FkkB+6ltgNvIzlwKpkO3IFkxklqO/AFkuGdZGa0JUhmXKN2GfgFKXrqyTRjIk3KQnDTKonhH1IZWEFmLEyjNKPh8/0fpBBsJ9OBC5BbQNR24Eck388m04GrkBy4m9oOfILcxCLTjKNInlxBbQdeRTL8EJkh+AwpHTdQu5jZQEkRAerpoWQ0GtTFbSh1MRENVbQYcENpLFC70Qp0YBRaUz+YbA+CopqEtiSxtYA4uLmOsGQxiV1U5KpyFq1a1IeRLHn3v4GBhazoTWbwoI0DUxgy0SxKIqolnsxwHknPeapHL9yyBgY2oCX3kRz5/n8mgxKBtNeN4qkUBn9adzsj0ELxGTCUbDDUFTAIAOVnoqldTq8Bo24sQxtHgeITQE15aO/vI5rjzgM9wkW/gaMUhi6UXI1/8Ggt0HH8AzH0ZgdtJf/B4bjDQI9EDvwYIaiMBBUlKQyJQJwMxK6gcUOGUTAKRsEoGAWjYBQQAwCLecmDRDeOKQAAAABJRU5ErkJggg==",
                            0)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAA/UlEQVR42u2YPQrCQBCFnyIIouANPIJnEMHOwitk/QFBsLH3DhaCYC8IaTyD4CmCWKw2egAL1zEEiSEpROJGeR88ttjAfMwOCxuAEEIIIX+BGaJsHEyMwkpWV9Z2duQctCQHiXlGYZYFsYqILF7EsiIoAk3JPlbOpqA/a13MY6TWInWxKiiFG1LYi3RKmx46QVe1NUEzQF0K3yJySzNGNXTsFgWnKEjhXVDce8xgzFxqu0esUPPvuj5KCfs6U9cMBSlIQQpSkIIUpCAFKUhBCn5VwCAnxTeSbWwUrqFn6THhOzc9wRGKib863kiab+O8dOn8kaDCCYQQQgj5Ge6pY9Iyj7oxvAAAAABJRU5ErkJggg==",
                            1)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAB1ElEQVR42u2YTShEURTH/yGf+QoLLMQaCxZ2kiwsJ9Y27z0iTfnciUlZWs6IYq2ZZCNZGClZWJiFkpTsZCHZmBLpOm+Geu+axpPezFHnX6dT7y3u7/3vuefdewGRSCQSiURepCZRp0Io4gEzhkZlYEaZ2KR8SvmRsqL8SvmK8i5FIPdgJlooIhQvKaCf44Q+pic3cBZWCOwtC8xzxuf2x1jo9h/QwIFj0CTldRp4iBzqoCj/nPp6etZP7+Ia5K2aQo3f09tOkaAB59UEaj04PqtBbvBb2Sb2HIA3HAEDLhc9OJ9bQAutLsBRDPACnEaZVoeD3Ka4V2s7TdwAFx3uPfCCs3+HX7/AdGxxbjFJNY5mTqs3pNXeMifnFrSVe0TbsGIecAaWNOcuqBar8w8WQgE5Fdacu2ZRd/b0EVBMc+5cBdHAAa6UYPa/1ZyByvzDzaGCYA4152LkXEn+4aIodG1g085F7Frk0kpWXXAW1jj1uRHNuSgf54Kocv1fTZyxacIZdifv1IS7+MDRqYzAnhyAYV67EwPD2vmije/m08A9xxPatmN6dzgCJhyAd6lLo9/HsX2/41cNXnq8MMoeFvr8Aoz/Gc6+nrPQCZFIJBKJRP9GH3cBH0EPXPAiAAAAAElFTkSuQmCC",
                            2)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAACAUlEQVR42u2YTShEURTHj49IplAKYUGSBTtloWTHjhoLxkZzZyZEDc2CbJTsFErKwk6SBSVlxY6ysyALkZVJk/KxwPi4/u+ZdOeZyQyeOer+6/Rmppnur3vPued/hkhLS0tLS0vrX0i6aRix9u0QtCL7qNI+QEH7WEj+KAS127mD278AWGUf4CBlSw8VJRw+KgHUrgJ3yCtnPTSmwIUB3MAJrhlgzwpggA9cPxUDKqjAbUhJaTzgVikDQDsK3DmulgI+uydogm/eeakFYK8fgB7ycyqKMuxYSNm9dT5w45Rpue/OpJ/yOfXoKaVbPCLqORVFa1Qr89AIN4dzYOm3RpEcAXwRTy+quDDVgDdfmIMQwpVKQCcABkyvKGjG7BpuOo0BuikDlMvj2NHWkIsd5lFHg87yyk+j7akeUtALopEX5GfjsMdvlhE0qhzzvbGz3ACj70of1XA8ZrVYnLwAe6nUcuU0cZtN2ixHnMcNcFJ1OrzgvFQNqDvFTCzZWY21WKAiieJwRJkJQdf4rNyuSnRggSvTrbwPRd1yiHLiGNh0fMeNuLBUb4+du+eKYQDCiBPEFt7P4Tkfgb+M4WyW7c0ljI5YZAEAt0n+HxPEbzr/LulhmZCHwpxBBD3E8YBPeB4jplN6pURslTHNNRn5hdddAKpDDmaRlpaWlpaWVqJ6A5j3Th5Dv/jyAAAAAElFTkSuQmCC",
                            3)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAABgklEQVR42mNgGAWjYBSMglEwCkbBkAf/kxik/iczzAfS68A4mcF6cDkwmWEV0GH/wTiZ4e//dAbVweQ4eaDD/iA5cONgC71euONAOIXBYTClPV4g/ogUeucGV+ilMBSghF4yQ+zgcdwqBmago+4jOe7Z/wYGtsEUeiEooZfEUDXYyr6jSI779j+bQXjwOC6NwRwt7c0YbEXLCiQH/gM6WGMwhZ4c0IG/kUJvK1Z1wCj/n8qgBsYlDNz0DL1ulOhNZXDBoe4Kkrp59CuYkxk+IIXeRTxq7yKpW06voiUPrVpLHDQOBBbCTGiWvvify8A+eByYwhCEVrTUEUgOdHZgEsNhJAd+B4ae6KBxIDCnmqKF3mwiPERHByYzLEUrWrQHjQP/FzAIAC34geTAR0CcSgR+haTnFIpcCoMeNaPXHq3VQg28jHa5lxoYWJdTu+6dCmqxkIRRuwJ30OR9B0N7kf5V3agDRx046sAR5cBkhmNIDpzAMApGwSgYBaNgFIyCIQkADPArw79NMFwAAAAASUVORK5CYII=",
                            4)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAABp0lEQVR42u2XO0sDQRSFj69C1CKdgoW9aCFiJYqIj16xCVjsbGKSTlmEoEUKf4CFiIiNjYiFiNiIna2NWEl+gKBiIyhi4XgyIbJZiEggmyvcA4ebVPNxZubOXUClUqlUKtW/kU2jzxocWw+nddlgpbGAKUxyIVu3DS6lA57HC8gtsz6G/+wsEvEC+piRdUkUUAEVUAFjBjTIEzJNb/D3lmvcHqb4v99atDQf8Hc/EjhnC2iXClhJ+Z6ej2vcGuKiRfqK3uHC66wZOulsENC79EsE8pN1VM5ZXUWnAy+DVUCLNoduWZeqnG44yX15U7iHoxDkeyldWYAG01UppjAnC5DnLjrkyttmg7cQ4Ka0L8GOyG1ek/Y8Dkaad1Ia4FLkDI5JO39nVe9zAa1y4HwsRtLblpTchEss/NQF6Grkgnn6gd5zzfcEbTX6Xi9hDuiv2IaF0kzHRZ4j2/XKesd6wXrIes36VGPkChr9nvbQt3XMgjfsg+PxnCmO8KXB06VVK6ky1IdL1sdyU8b+H+AsEkxnhDAL7rvEw6zNYEBUG1GpVCqVSr6+AVKhZ3NdZ6LIAAAAAElFTkSuQmCC",
                            5)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAACnklEQVR42u1YzUtVQRT/VRYRIRQUhWAIbaygqE1FVIsMXLUpgxZp7973LEEMatEiQWoblbUKl9KiRxH+B30QWVhiPGz5FkZRYVloRR9Ov5nX07mj8j5qplnMgcNc7p1zz2/OOXPOmQECBQoUKFCgQNWQOIVVohO1/gCKsV9EuEkeEil8JAvFET6Tc3y+xDk7hcAit8DSaKbypzOASnOvS6udocLpOSAi/OCY5/ia40/jW5cbcNJlSWBfyTdo0W0iiyUz8/hMUFu4mHMcB8RJrHVhucMGuLxox1Y/NgMtQEDvNHCvRAfW+bNbI/Rp8fSL4y6fwK0moC8awAG/km+Es4nYS2OfXwBTeKFZb8gvcJ1Yk8h5EWK/AKbRknBvOzb55t4rGsBJ0YPF6j1rK8FvZm48SqteJGfJl8mtKmn3YJkrgLc0gA9FBks5thHIaIna+4lzjrkAeE+Lv1HyWAUNgpTpt9p+UcnLBZTnVQ2W1kzhIN26m+4+oUIiwoQB8q7NHPjBUDZCPlKMxXllMqjnnPsJuQz2/HtwhXibVRKju9zGU7qVIN9r8oM2GoQ6o4K0VGj9LsP6G23UYN2CmQo9sN5YYLONGPymu7gK+SmrXfWf3VpUcL0KgDltgddsAHykAcz+FUBWHBsuvq0n6irkx7U4bLNxDuk28tn2smU7sNLYxXvtpJrCcbKo5GoF1mtNAOS/bFWTO5qit/J6o0y5B9rCctZuF5i/Dhh1+FkpkHOSdIxD9uqx7P0iDBvx9FwdzA2rMEZX8P15Y+5jFwenDVT2Zp52alzdHDBHkp+QvxvfJ+Tlkas7mUZlufL7wUHKNLg9AsgOJ8bpBYHKHV9IzhfYktX877vBBnIT+bhqXGPsIKjlCBQoUKBAgQKVot9xB9F2ms19aQAAAABJRU5ErkJggg==",
                            6)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAABd0lEQVR42u2XPyiFYRTGH4VEBoPBRphEMVmYKKOIrL7vu/cyMN3J4g66YrKxmGyKbFfEaDKZTEb/stwSSbxO1O1xN123HjpPnfV7fz3nPO95P8DlcrlcLtefUEhhJMTYCxH2K6oEE9UBjJG3A0LFFWOrOoARln8F0L5THcAcGkIavdaivh9VGgMGVSQHx7RmN8Y4ufdgwHVqgAVyb0MLbg7tBvVWAkyhR829FXLvTAsuh1qDui4BJpjVArQLmcJRtHA0agFGOCT3NtXc6zCwd5q/frVw5AnuXAvOLmIDuyHAjNrsTVI4Hq2a1dp7RO5tq7W381s4EgyqtXeV2nuhF44Yt+TeotrsTZF7z2EeLWrtPaZw7Ki511W2OYbVANcI7lLtWVVvYHcEmFX7X56mcLyEBbSqheOE3NvVgsugu2xzjKq9+9bJvasQUKMWjnuavyW1q2WG3Hu1VdemBnhKgAdacFk0fe7bL8Anm8UhuFwul8vl+pf6AFaOsSRnLOG6AAAAAElFTkSuQmCC",
                            7)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAC2klEQVR42u1YTUhVQRQ+Zb8WFESLKAKpqGgREhRWCgWt+tkHFXXvMx+F5tOKlBa1EtGtiwgqKFJcBNGqoiJEKjdthCICpcBCo4Toh6ymb+4znXuauvOeM9cWc+Bw37t35p5vzsx35ptL5M2bN2/evHkzNXGclohzNOf/AZShTSKkKyKgPvgHuMD/H7gO4HoHz+sAeF76wGpoIwDcjAAleUhD8BNC0Ix0wIVUDv+kAfIW/gg+iP8/NWBb3IM7SssA4LUC6iuu7aKWlrJ2pXiWg79nIA+7BRjQXQXcGIBsT1ijZRNrM+9fRD0tdkWIMjalZw2XxEHWL3AF8FAsUIbWG/VDxtg0t7siR5MS5HMhrET7V0oGL7oCmItlAoQx6oc6iL7flMw3ugK4l03xAcPs7WD9drkqMaUIMKwEeyOytPyffXI0HwPrV6b3JUrSXJdb2xnGyKeS3dq2x2gh2txgBNnntg52UwlAdTGQH3HNYq3NUrK9BfeesXat6Wx1AIKAlzTb2QC8AX6L3f8uM5++YAioAll5kiAUHopq2jA9UquatgJEb4KaeQc/JcmSHrA8Mzs12bofgQnpuebZoJRoaaiZRQjYwwD0SlJMtMEOI9kKf/EHmVBL3TI4oMcsaJvKXk2m28YV9qSaqaE1rkiRZeCaDGtnPev3wLq6jqY2pBElUA8yN7OAbfIy2+522mbsHhagssABrmNZPG17eptjhRf7chFiY0QB2GUb4HXl5f1FvuOeWsBti4Rr6umtSLk2pLyj07YWbI2toQSZpVmDK1ntbLYNcPdUFjn6XGD9V9suM7MjgToZQEr4CsPBZRg4Z2eSqtjZIqTRiN0nacFfBrVqfM9WZVmfW0XNd5PfiiWk2/AO+Hn41QiIOpi8D4NsK9L4olXHziZJH4/GojVoeAq0AxLTKokSW5f6zHbjunb6P1qGtBmZ3Q8/gt/b5D3/OdebN2/evHkzsl+u8jSnE9FcQQAAAABJRU5ErkJggg==",
                            8)
                    .put(
                            "iVBORw0KGgoAAAANSUhEUgAAACgAAAA4CAYAAACPKLr2AAAClklEQVR42u2YT4hNURzHv2OBhAgj/xULSUksLIhSlrKwERbuvXON0RQL2ZjSULNi4V+NhWxEGmHBFGEa8mdhJ81obEzKsxLSZHB8731v8rvHm9u7T783Z3F+9eu87jun3+d8zzm/87sX8ObNmzdv3rzVYqYdU+jzzAlMcgOoFctNgC4T4g7bt/SfdFNp3/P5Jfq2iVEqQgchvleA8j3EbRNjQWPg2rCEAYeqQPxi+47t8DiQA4Scqw8Y4r4V+DkDb+K+myqWfhEV3pUCZ/s+0oUL0GoFPG4MmsbtH2E++72y1NyhtbTTCfRVBOrLgxOTmkEviXEvdAAjbLTUW1NA+S4x9jd9oQZgLOC+FE5H5UM0Btmisf/OC8CXdRyuZ2L8OY3T2ysC3Khj/DWh4AMNBS+LAE/qADwjJjiksQc7RIDhOiZ4VEywpKHgHiufbSio4D4x9pvWFTciVOyteWyMpex/V4wd1brmTlu58Lo5iNk522I9+3VXKSo+6QAewhxCfbaClfish20n27YkhdD7q/STE3ujdx8nN0qIwZrKrL9APdYS9+sWDTGmEfSsdTvYUKP0W+y3uXLIHor/bjamNmSJZQ5gLSF2M+hJ+sW0wgmxk0XtTCsLyNKr2613Fb6jEPqHUPCUW4Dldxe5/JFbgAHaM4AxVrkFGOKp6jX3n2lpcaVIHVveq66pdySzvC3Y4g7cYcwi4Aeh3mvX1LuSUS/CXpfy3gX764J+YFYuaV0XYlnOodjK/x9bcB+ZC5sbsWTHMp8ykm8uiVJJOV/+PVDlTh6kr2xUwu0rVMUEuJdXL2oouC59/ZQn81/FRtLDwb4TdwgMmljFrCbQdu65/YQJku+AvMJW8IBMhjdv3rx58+atiP0BIknE3ok8b5IAAAAASUVORK5CYII=",
                            9)
                    .build();

    Map<Integer, Integer> toPinKeyboardMap(List<String> images) {
        if (!IMAGE_TO_VALUE_MAP.keySet().containsAll(images)) {
            throw new RuntimeException("Couldn't find mapping for all values");
        }

        AtomicInteger counter = new AtomicInteger();
        return images.stream()
                .collect(toMap(IMAGE_TO_VALUE_MAP::get, i -> counter.getAndIncrement()));
    }
}
