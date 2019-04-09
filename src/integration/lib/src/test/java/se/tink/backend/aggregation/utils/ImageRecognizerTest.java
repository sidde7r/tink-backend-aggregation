package se.tink.backend.aggregation.utils;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ImageRecognizerTest {

    @Test
    public void testIngDirectImages() {
        ImmutableMap<String, String> data =
                ImmutableMap.<String, String>builder()
                        .put(
                                "0",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAAA+ElEQVR42u3awQ2EIBCFYSu0AAvwbgEUQAEU4N0CLIAC7IICuMPmkejB7GGTzbqI/yRz0NvHDGGIdvlh0QEGDBgwYMCAAQMGDLhecEqpJGBa+mbgmioJmJYGDBgw4A9iWZZsjCk5z3MOIbQLnqYpD8NQoIKP41ier0RfBl7XNfd9n7dtO97FGAvYWtseWChV+ByqthaiOfC+b9/tacCAAQMG/G+wjh+duTp7zwuhAaQ5sKYpVdI5d6D36mooaXK09N6XKgu5p9DN35Y0XirP7c31EDBgwIAB/zBq/BIBmJYGDBgwYMAVge/w28I35z5g9jBgwIABVxQvphewyrRVN/MAAAAASUVORK5CYII=")
                        .put(
                                "1",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAAAr0lEQVR42u3awQnEIBAF0FRoAekiBaQjC7CMFOA9BXjXRW+5LQtZiHkfBsXbg1FQXNrLsgADAwMDAwMDAwMDAwMDAwMDAwNPBq61jgL+Iud5thjjqJTS/C2dc277vrd1Xcf4mj3cscDAwMDAwMDAt6aU0o7jaNu2jerzvjYtuANDCJfqa66HwMDAwMDAv+Vpj3rAWhr4vvxjewDPfnABO7SAgYGBnwB++tcG4Le29AdqMtjDWTPVJAAAAABJRU5ErkJggg==")
                        .put(
                                "2",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAABDElEQVR42u3ZwQmEMBCFYSu0AAvwbgEWYAEW4N0CLMMCvFuAd2d5gQFdcPcgZNnxHwiity95kygW9rAqAAeqfd/TAEykAQMGDBgwYMCAAX9/dfz0/G/BDnocmB4GDBgwYMA3ats2G8fR2rZNYxgGW9c1LrhpGquqKkEFr+s63edEZwNP02RlWdqyLKcV9wkIB+66Lq3we3m8w4GvYIq1JiMcWFE+xlmlPlbMFffwx5Lw6t+ccf4ZWLuysOppbVyhwQIKqt7Njf0J2M9i7+d5nlMvhwT3fX/CHjeucGCH6apV9aFJCAnWbizY1eBrCTBgwIABAwYMGDDgIOA7/2QBE2nAgAEDBgwYMOCA9QKEjLMxUGTzrAAAAABJRU5ErkJggg==")
                        .put(
                                "3",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAABG0lEQVR42u3ZzQmDQBBA4VRoARZgFxZgARbg3QIswALswgK8u+EJC0ECgZBswvgGFrx+zt+Kt3SxuAkWLFiwYMGCBQsWXBi87/txBFvSggV/Kt5pM8GWtGDBggU/xDRNqeu61LZtGoYhresaFwywqqrU930axzE1TZPqui6KLgYGBRZojm3bDjAvIhwYKGCQj0Fpc8KB6d1nMMqang4/pck0vUzWl2WJDQYI9NzTYcFkd57no5RB83yZiwd9TR+HA7N6nq2fPL3DgSlfdu55LTG4QmaYPs23rIzO2S05uIr2MLuYLOcJzSl5y/rpWuKcy9vPQ8GCBQsWLFiw4Ffxzz/Pvwr+R/TXSvpyYIeWYMGCBQsWLFjwlbBcde+jNLjIUJm5KgAAAABJRU5ErkJggg==")
                        .put(
                                "4",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAAA7klEQVR42u3ZwQ2EIBCFYSukAAugCwuwAAvwbgEUYAEU4J0CuMNmSNgYb3vYdZ35JyExxsuXeY4Qh2qsBsCAAQM+VymlLcBEGjBgwIAB2wB/8ik0BZZnTEXaHJihBRjw809OgIn0lyrnXKdpqiEEG+B5nqtzrm7bph+873vDjuOoHyxRFqiZDq/r2sACVw+OMTakRFpKPdh73yZzL9VggUmUU0o2wH0qS4f7knvXrqvq8HUJWLC/7PKtW0szG4/jON4Te1mWdq0a3N/f8+K0dAf43399AibSgAEDBgwYMGDAgAEDBgwYMGDAgAE/v15ZjLViKxGyJQAAAABJRU5ErkJggg==")
                        .put(
                                "5",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAAA7ElEQVR42u3awQ2DMAxAUSZkgAyQOwNkAAZgCwbIAAyQAXJnAO4JcioqKtFTVUScb8kS14cdO0h0ubHoAAP+PVJKJQHT0oABAwYMGDBgwIABAwYMGDBgwLWB53m+TLXgvu+ztTY75z5SNfjuigK+GzxNUw4h5BhjG+BzGmOy914veF3X97NUeBzHAl+WpZ09PAxDyWbAMsSkyurA3ya0WrC07dUlQ21LyzQ+V3nbtrKiVA+to32bWUtHZeXiIcnnIWDAgAEDBvyKJ/1LVTX4aS/y7y3dHJihBfjeIwKYlgYMuKrzDJiWBgy4qtgBBnq6dXQ8yfsAAAAASUVORK5CYII=")
                        .put(
                                "6",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAABDUlEQVR42u3YwQmEMBCFYSu0AAvwngIswAIswLsFpAy7sADvRl5gFhT25kZ28g8MiKf9nORltUmVVQMYMGDAgAEDBgwYMGDAgAEDBvxqHceRGzBLGjDgV8D7vqdlWdIwDLnneU7btvkECxtCSH3fZ7Ra113XFUUXAwvYtu0Fp4ege9M0+QNrCWvC94ox5nYJVlcTWgbWNMdx/ISWlrVbsParQkpQ96Fl4HVdL6ElsNvQ0kTvpWnrQVQTWnZcuQPbJO8h9e24+nuwgsn2q6Ftui7PYZUCS/tYSGuh3b8tCa4ufQbzelgV+Bffj56uJ35jvWD2MGDAgAEDLnf8AGZJAwYMGDBgwIABA372L+sJXt2sjAHS2MUAAAAASUVORK5CYII=")
                        .put(
                                "7",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAAA7klEQVR42u3ZwQmEMBCF4VRoARbgPQVYhl1YQApIASkgdwvwbpYXWJCFhT3sbjTzDwyCJz9mMkPQFWPhAAMGDBgwYMCAAf8SfBxHzavHN77TJpgzDBgwYMCAAQMGfGdwCKGs6/o2uwPP81zGcazPc+rdMAx9gpXn2Pe9gpdlsQFWm6u6Oef+wEK9wqZpKt57G1M6pVSrqyqbAD8Hlok9vG1bre4/11FTsKaywIJ3D26xipqC1caqroaWCbBWkdLE5SHG2GQVcVsCDBgwYMCALwy+y/9iwLR0x+BPu9LdHWAWzBkGDBgwYMCAAQNuHQ8lxsiacsyLLwAAAABJRU5ErkJggg==")
                        .put(
                                "8",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAABJ0lEQVR42u3awQ1EUBDGcRUqQAHuClCAAhTgrgAFKEAB7gpw9zbfJC8R4bAra+34TzIHbj8zb94TkvCwSAADBgwYMGDAgAEDBgwYMGDA+7EsiyVgWhowYMAn4pPZcjm4bdtQlqVl0zRhnme/YCHTNA11XRs2yzLLM+jbtnTf94ZVhWNM02T3hHcHFlS4vaor3YG7rjOwqrqOPM+txd2BtU6LorAUWtdxHY/j6HNbElRAVTqm1rbLfXhdYSGHYQhVVfmtcGzf7RakgaWH4A58NI2Ppvffg9W+msjb0IRW5d2BtWbjKSu2dazu+jDiakprL95O6StPWT97W1K1lVeeoXkfBgwYMGDAgAEDfhz42x/uANPSgAEDBgwYMOBbgD38r5W8g/QAfgGxvKj5CyEq0AAAAABJRU5ErkJggg==")
                        .put(
                                "9",
                                "iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/NlyAAABI0lEQVR42u3asQ2EMAyFYSZkAAagZwAGYAAGoGcAxmAAegagJ6cXKRJCue7gwP4tpbgUnD5sByNRBGdR/PJi+77H5RL8VHxx1YXdgV2UNGDAgAEDBgwYMGDAgAEDBgwYsD/wtm1hGIbQtm1c4zjaBa/rGqqqCnVdR6jg+i24SXDf9xGoLKeY5zmUZRmmabIHFlZZPUfTNKHrOntgZTLXs6mfzYHVu2eYyvvuPr4NrD49ZllY9bX2TIIVwgqYlrKey7ypwUOZ1em8LIvtHv4WZsEqYfVs7nGV2zczeKicz4dW2jMFFlADxvHQ0g24c8r6Sw9rplZG78wqr4eAATsCv+HbKxPgq/73sSXtDsyhBRgwYMCAAQMGDPhNYGuvgoDpYQfxAbl8q5kDeomEAAAAAElFTkSuQmCC")
                        .build();

        data.forEach(
                (k, v) -> {
                    Assertions.assertThat(k).isEqualTo(ImageRecognizer.ocr(v));
                });
    }
}
