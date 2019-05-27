package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.junit.Test;

public class QrCodeParserTest {
    private static String ENCODED_IMAGE =
            "iVBORw0KGgoAAAANSUhEUgAAAjAAAAIwCAYAAACY8VFvAAAY60lEQVR42u3dwbHj"
                    + "MAxEQeWDaJx/MHIWFDHTr0pn/aVIsPfk55UkSVrWYwkkSRLASJIkAYwkSRLASJIk"
                    + "gJEkSQIYSZIkgJEkSQAjSZIEMJIkSQAjSZIARpIkCWAkSZIARpIkAYwkSRLASJIk"
                    + "AYwkSQIYSZIkgJEkSQIYSZIkgJEkSQAjSZIEMJIkSQAjSZIARpIkCWAkSZIARpIk"
                    + "AYwkSRLASJIkAYwkSQIYSZIkgJEkSQIYSZIEMJIkSQAjSZIEMJIkSQAjSZIARpIk"
                    + "CWAkSZIARpIkAYwkSRLASJIkAYwkSQIYSZIkgJEkSQIYSZIEMJIkSQAjSZIEMJIk"
                    + "CWAkSZIARpIkCWAkSRLASJIkAYwkSRLASJIkAYwkSQIYSZIkgJEkSQIYSZIEMJIk"
                    + "SQAjSZIEMJIkCWAkSZIARpIkCWAkSRLANPWb8Rx6bv6mylvrU39b2nvMUXMUYADG"
                    + "AzDOD8AAjAdgAMbBc/AABmAABmDMUQGMgwcwhgLAAIw5ao4CDMB4AMb5ARiA8QAM"
                    + "wDh4Dh7AAAzAAIzHrAIYBw9gDAWAARhz1BwFGIDxAIzzAzAAY74BDMAAjIMHMAAD"
                    + "MADjMasAxsEDGEMBYADGHDVHAcYA9gCMAAZgzFGAARiAcfAABmAABmA8ZhXAOHgO"
                    + "HsAADMAAjDkKMAawDbRwaKf9bf494/zAlTlq3QDGBgIYFz7AOD8A4/4BGBvIugEM"
                    + "wAAMwJij1g1gbCDrBjD+PQADMOaodQMYGwhgXPgA4/wAjPsHYGwgGwhgAAZgAMYc"
                    + "df8AjA1k3QDGvwdgAMYctW4AYwMBjAvfv8f5ARj7AGBsIBsIYAAGYADGHHX/AIwN"
                    + "ZN0Axr8HYABG1g1gbCAD2IXv3+P8AIx9ADA2kA0EMAADMABjjrp/AMYGsm4AAzAA"
                    + "AzDmqHUDGBfxzsvu5m/qF7nBwgzJhJJ1AxjDx8EDGIAxTAHGHAUYgDF8HDyAARgz"
                    + "BGDMUWcOYAwfBw9gAMYMARhzFGAMHwcPYADGMAUYcxRgAMbwcfAABmDMEIAxRwEG"
                    + "YAwfBw9gAMYMARhzFGAMHwcPYADGMAUYa2COAozh4+ABDMCYIQBjjgIMwBg+Dh7A"
                    + "AIwZAjDmqADGwQMYgAEYMwRgzFGAMXwcPIABGDMEYMxRgAEYw8fBs9YAY4YAjDnq"
                    + "zAGMg2dgWQO/eg1x5ijAAIyD5+ABjL0DMLJ3AAZgHDwHD2DsHYAxR50F+w1gHDyX"
                    + "tzUAGIAxRwEGYBw8Bw9g7B2AcaE4CwADMA6egwcw9g7AmKPOgv0GMA6ey9saAAzA"
                    + "mKPmKMA4eA4ewNg7AONCcRYABmAcPAcPYOwdgDFHnQWAARgHz+VtDQAGYMxRcxRg"
                    + "HDwHD2DsHYBxoTgLAAMwDp6DBzD2DsCYo84CwACMg+fytgYAAzDmqDkKMA6eg+fg"
                    + "vdbt4Lo5C9bAHAUYgAEYBw9gAAZgAMa6AQzAOHguYuvm8rYG5qh5ADAA4+ABjHVz"
                    + "eVsDcxRgAAZgHDyAARiAARjrBjBy8Bw8gAEYgAEYcxRgAMbBAxjr5vK2BuYowAAM"
                    + "wDh4AAMwAGMemKMAAzAOnoMHMAADMABjjgIMwDh4AGPdXN7WwBw1DwDGxeXg2QcA"
                    + "AzDmgTkKMADj4Dl4AAMwAAMw5ijAAIyD5yK2bi5va2COmgcAU3JxWbfxHsgGMutm"
                    + "jlo3gLGBAAZgAAZgzFFzFGBsIBsIYADGRQww5qh1AxgbyLoBDMAAjHUzR60bwNhA"
                    + "AAMwAAMw5qg5KoCxgQAGYAAGYMxR6wYwNpB1AxiAARjrZo5aN4CxgawbwAAMwACM"
                    + "OWrdAMYGAhiAARiAMUetG8DYQNYNYADGRWzdzFHrBjA2kHUDGIABGIAxR60bwNhA"
                    + "AAMwAAMw5qh1AxgbyAYCGIBxEQOMOWrdAMbA6noAxnu8x3s8uZAFGIBx8Ax67/Ee"
                    + "gPEADMA4eADjPd7jPQADMAADMB6A8R7v8R5zFGAABmAcPIPee7wHYDwAAzAOHsB4"
                    + "j/d4D8CYowADMB6A8R7v8R5zFGAABmAcPIPee7wHYDwAAzAAAzDe4z3eAzDmKMAA"
                    + "jAdgvMd7vMccBRiAARiPQe893uM9HoABGIABGO/xHu8BGHMUYADGAzDe4z3eAzAA"
                    + "AzCSXFwLf8XbxSUBjCSAARiAkQBGAhiAARhJACMBDMAAjAQwkgAGYABGAhgJYAAG"
                    + "YABGAhgJYAAGYCSAkQQwAAMwEsBIAAMwAAMwEsBIAAMwACMBjCSAARiAkQBGAhiA"
                    + "ARiAkQBGAhiAARgJYFwoft3Uulm3qss7bR/AL8wDDMC4HKybdQMYgPEegAEYgHER"
                    + "WzcPwACM9whgAMZFbN2sNcC48AEGYADG5WDdrBvAAIz3AAzAAIyL2LpZN4ABGO8B"
                    + "GIABGBexdbPWAOPCBxiAARiXg3WzbgADMN4DMAADMC5i62bdAAZgvAdgAAZgXMTW"
                    + "zVoDjAsfYAQwLhQXsXUDGIABC4ABGIBxEVs36wYwAOM9AAMwAOMitm4egAEY7xHA"
                    + "gFLJhWKQOD8ufDMkdV8LYAAGYAwF5wdgAAZgAEaGD8AIYADGmQMYgLFRDR+AEcCY"
                    + "Ifa1dQMYgAEYQ8H5ARiAARiAkeEDMLIPAMaZAxiAsVENH4ARwJgh9rV1AxiAARhD"
                    + "wfkBGIABGIARwACMAAZgnDmAARgb1fABGAGMGWJfWzeAMYABxjB1fgDGDAEYgBHA"
                    + "AIwABmCcOYABGBvV8AEYAQzAOHMAAzA2t8cvS3sM+qIL0h4VwACMB2B8UzMEYOxR"
                    + "gJHDCjAelwPAmIn2KMDIBQkwHpcDwACMAAZgPADjm5ohAGOPAowcVoPEurkcAMZM"
                    + "tEcBRi5IgPG4HAAGYAQwAOMBGN/UDAEYexRg5LC67HwflwPAmIn2KMDIBQkwHpcD"
                    + "wACMAMbw8QCMx+UAMPYowMhhddn5Pi4HgDET7VGAkcMKMB6XA8CYifYowBgKQGZg"
                    + "1ZwFKM2Due9jhgAMwACM4QMwLkiAMUMARgADMIYPwAAMwJghAOMitukMH4ABGIDx"
                    + "fcwQgAEYgDF8AMYFCTBmCMAIYADG8AEYgAEYMwRgXMQ2neEDMAADML6PGQIwAAMw"
                    + "hg/AuCABxgxxlwAMwACM4QMwAAMwZgjAuIhtOsMHYAAGYHwfMwRgDG3rZvg4CwAD"
                    + "MGaIuwRgAAZgDB+AARiAMUMAxkVs0xk+hg/AAAzAmCEAU3CpQsK47PzSb+T5cU7H"
                    + "7PWfYYABGIABGIABGOcUYAAGYADGYAQYgAEYgAEYgAEYgAEYgAEYgAEYgAEYgAEY"
                    + "gAEYgHFOAQZgAMYhMhgBBmAABmAABmAABmAABmAABmAABmAABmAABmAABmCcU4AB"
                    + "GIABGIMRYAAGYAAGYAAGYAAGYAAGYAAGYAAGYAAGYAAGYADGOQUYgAEYgDEYAQZg"
                    + "AAZgAAZgAMYANhgBBmAABmAABmCgx6+bulQN7ab/aHiP/9T5TxDAGKYAAzAA4xIC"
                    + "GHsHYAAGYAAGYJw5gAEYgAEYAYy1djkADMAADMAAjGEKMAADMC4hgLF3AAZgXKoA"
                    + "AzAAAzD2qL0DMAIYa+1yABiAARiAARjDFGAABmBcQgADMAADMC5VgAEYgAEYe9Te"
                    + "ARgBjLV2OQAMwAAMwAAMwAAMwACMSwhgAAZgAMalCjAAAzAAY4/aOwAjgAEYgHHm"
                    + "AAZgAAZgAMbAqvlFYYgDJb/InXl5Oz8AAzAAAzAuVYBxfgAGYAAGYAAGYADGWptv"
                    + "ACOAccABBmAMYGsNMAADMAADMADjUgUY5wdgAAZgAAZgAMalaq0BBmAEMA44wACM"
                    + "AWytAQZgAAZgAAZgXKoA4/wAjPMDMAADMADjUrXWAAMwAhgHHGAAxgC21gADMAAj"
                    + "gDGAXaoAAzAA4/wADMAADMC4VK01wACMAMYBd1gBBmCstfkGMAADI36xtuJC8e9x"
                    + "Tn0fmdcAY6M6EC58FyTAuITsN4ABGBsVYAAGYABG9pu9AzAAAzAA45z6PjKvAcZG"
                    + "dSBc+C5IgHEJ2W8AAzA2qgMBMC5IgJH9Zu8ADMAADMA4p76PzGuAsVEdCBe+f49z"
                    + "6vu4F8xrgLFRHQiAcUECjOw3ewdgAAZgAMY59X1kXgOMjQowAOPf45z6Pu4F8xpg"
                    + "bFQHAmBckAAj+83eARgbFWAABmAARu4FgNHSTWfQ2zsnv8/N+8A59R+A2/ebmQgw"
                    + "AGMw2jsA45wCDMAAjAxGh9X3ARj7AGDMRIBxCRmMDivAAIxzCjBmIsAAjMFo7wCM"
                    + "cwowAAMwMhgdVt8HYJxTgLHWAAMwBqPDCjAA45wCjJkIMABjMNo7AOOcAgzAuIAB"
                    + "BmAcVoABGOcUYKw1wACMweiwAgzAOKcAYyYCjEvIYAQYgAEY5xRgAAZgAMZhBRiA"
                    + "cU4BxloDDMAYjA6r7wMw9gHAmIkA4xJyqbrsZO8AmX1t3QDGpgMYgwRgAMZMNHsB"
                    + "RgADMAYJwACM82PdAMamAxiAkb0DMPa1dQMYmw5gAMaZAxiAcZcAjAAGYAwSgAEY"
                    + "58e6AYxNBzAuIQEMwDg/1g1gbDqAARhnDmAAxl0CMAIYgDFIAAZgnB/rBjA2HcC4"
                    + "hAQwAOP8WDeAsekABmCcOYABGHeJAMamAxiDBGAAxkx0lwCMTQcwLiEBDMA4P9YN"
                    + "YJIGo18h9mvHfukX5l1c7hLfFGAABmAABmAARgADMDadSxVgAAZgAEYAAzAAAzAA"
                    + "AzAA47Jzl/imAAMwAAMwAAMwAhgBDMB4AAZgAEYAAzAAAzAAAzAA47Jzl/imAAMw"
                    + "AGOtAQZgXHYAI4ABGA/AAAzACGAABmAABmAABmBcdvJNAQZgAMZaAwzAuOwARgAD"
                    + "MB6AARiAEcAAjE3nUgUYgAEYgJFvCjDQ4xdr6xBnrcc8sK/NeIABGJsbYAx6gAEY"
                    + "gAEYgBHAGPQAAzD2tW9q3QAGYADGoLfWLjv72owHGICxuQHGoAcYgAEYgAEYSwAw"
                    + "LlWAARj72kUMMAADMABj0Ftrl519bcYDDMDY3ABj0AMMwNjXZrwABmBcqgADMPa1"
                    + "ixhgAAZgAMagt9YuO/vaN7VuAGNzA4xBDzAAY1+b8QIYgHGpAgzA2NcuYoABGAGM"
                    + "QQ8wAGNf+6bWDWBaNqpfIR5/W+AvMfuF8fF9AmEugAEYgAEYgAEYswpgBDCGAsC4"
                    + "7AAGYMwqgBHAGAoAAzAAAzAAI4ABGIABGIABGLMKYAQwhgLAuOycBd/UrAIYAYyh"
                    + "ADAAAzAAAzACGIABGIABGIAxqwBGAGNoAwzAOAu+qVkFMAIYQwFgAAZgAAZgBDAA"
                    + "YygADMAADMAAjADG0AYYgHEWfFOzCmAEMIYCJLjsAAZgzCqAARiA8Quvho/9Zr8V"
                    + "fVPo8bcBjAvFheKw2m8AAzCQYCYCjAvFhWK/2W8AAzAAI4BxoQCM/WatAQZg/G0A"
                    + "40JxoTis9hvAAAwkmIkA40Kx1tbAfgMYgAEYgAEYFwrA2G/WGmAAxt8GMC4UF4rD"
                    + "ar8BDMBAgpkIMC4UF4o1sN8ABmAABmAAxoUCMPabtQYYgPG3AQzAuFAcVvvNfgMY"
                    + "gDETAcaF4kKxBvYbwAAMwAAMwBg+LhT7zX4DGIABGIABGL8SvXBo+5VovxINI9qw"
                    + "dwQwAAMwAAMwACOAARibG2BcqgADMPaoGQ8wAGNzAwzAAAzAAAzA+KYAAzAAAzAA"
                    + "AzACGICxuQHGpQowAAMwZjzAAIzNDTAAAzAAAzAA45sCDMAADMAADMAIYAQwAAMw"
                    + "AAMwAGPGAwzA2NwAAzAAAzAAAzC+KcAADMC4HKw1wLjsAEYAAzAAAzAAAzBmPMAA"
                    + "jM0NMC5VgAEYe9SMBxiAsbkLLgfZB+CX+X38grUzBzAAYzDab/YBwAAMwJi9AAMw"
                    + "DhHAAAzAAAzAAIwLBWAcIoABGN8HYJw5gAEYg9F+sw8ABmAAxuwFGIBxiFyq9gHA"
                    + "AAzAAIwLBWAcIoABGN8HYJw5gAEYg9F+sw8ABmAAxuwFGIBxiADGPgAYgAEYgHGh"
                    + "AIxDBDAA4/sAjDMHMADj4pJ9ADAAAzBmL8AAjEMEMPYBwAAMwACMCwVgHCKAARiA"
                    + "ARiAARiA8Z6S4QPMfvU6EQn2gQAGYLwHYAAGYAAGYAQwAAMw/jaXnX3tARgBDFgY"
                    + "9ADj4gIY+0AAAzDeAzAAAzAAAzACGIDxHn8bwACMbwowAhiwMOgBxsUFMPaBAAZg"
                    + "vAdgAAZgAAZgBDAA4z0AAzAA45sCjAAGLAx6gHFxAYx9IIABGLCABIABGIABGAEM"
                    + "wHgPwAAMwPimACOAceEb9P42l5197QEYgJFKgKm7v2kaeoDsjVtr3wdgJAfcNwUY"
                    + "gAEY8w1gBDACGIABGIABGAlgBDAAAzDmG8BIDrhvCjAAAzDmG8AIYOQSAhiAARiA"
                    + "kQBGAAMwAGO+AYzkgPumAAMwAGO+AYxcdg44wAAMwAAMwEgAI4ABGIAx3wBGcsAF"
                    + "MAADMOYbwMhl54ADDMAADMAAjAQwAhiAARiAARjD1FPx69qG6ZgHzmnkOXV+AMbA"
                    + "8hiMAGMeOKcAAzAAY2AZjAADMOaBcwowAhgDy2AEGAPYPHBOAQZgDCyPwQgw5oFz"
                    + "CjAAAzAGlsEIMABjHjinACOAMbAMRoAxgM0D5xRgAMbA8hiMAGMeOKcA4/wAjIFl"
                    + "MAIMwJgHzinACGAMLIMRYAxg88A5BRiAARiPwQgwAOOcAozzAzAGlsEIMABjHjin"
                    + "ACOAMbAMRoABGPPAOQUYgAEYm866LVyDNFxZN9/0diiZowDjErJu1gBgAMY3BRj3"
                    + "D8DYQNYNYAAGYAAGYADGwLKBrBvA+KYAAzAAAzAuIetmDQAGYHxTgHH/AIwNZN2s"
                    + "gcsOYAAGYADGRWwDWTeA8U0BBmAABmBcQtbNGgAMwPimYOHMAYyL2LpZA5cdwAAM"
                    + "wACMi9gGsm4A45sCDMAADMC4hKybyw5gAAZgAMYcBRgXsXWzBi47gAEYgAEYF7EN"
                    + "ZN0ABmAABmAABmBcQh8OHwfc5ZB44VvrvO/j19zta4ABGIABGBekQQ8wAAMwDjjA"
                    + "AIzhAzC+D8CYIQADMAADMAADML4PwNjXAAMwAAMwLkiDHmAABmAccIABGMMHYMw3"
                    + "gAEYgAEYgAEYgAEY3wdg7GuAARiAARgXpEEPMABjXwMMwACM4QMw5hvAAAzAAAzA"
                    + "AAzAAIzvAzD2NcAADMAAjAvSWgMMwNjXAAMwAGP4AIz5BjAAAzAAY9B7j+EDML4P"
                    + "wJghAAMwAPPxe1wocOXXmzN/Xdt/ggAGYBxWgAEYgLHfzET7DWAABmAABmAABmAA"
                    + "BmAABmAAxoViMLpQAAZgAAZgHFaAARiAsd/MRPsNYAAGYAAGYFze1sBMdE4BBmAA"
                    + "xoViMLpQAAZgAAZgHFaAARiAsd/MRPsNYADGYQUYgHF5WwMz0TkFGIABGBeKwehC"
                    + "ARiAEcAADMAADMAADMAADMAAjMMKMADj8rYGZqJzCjAAAzAuFIABGIABGIABGIDx"
                    + "K9EuIb9C7Pv4PpAAMAADMADjgnRBWmuAEcAADMAAjAsSYHwfgBHAAAzAAIwL0vfx"
                    + "fQAGYAAGYAAGYFyQvg/ACGAABmAAxgUJML4PwAhgAAZgAMYDML4PwAAMwAAMwACM"
                    + "C9L3ARgBDMAADMC4IAHGAzACGIABGIDxAIzvAzAAAzAAAzAA44L0fQBGAAMwAAMw"
                    + "LkgXpAdgBDAAAzAA4wEY3wdgAAZgbG6AybvsnAXATNwH1kAAY6MCDMA4CwADMM4C"
                    + "wLiIrRvAAIyzADAAI4ABGOsGMC4ugLEPAEYAY6MCDMA4CwADMM4CwLiIrRvAAIyz"
                    + "ADAA4ywADMBYN4BxcQGMfQAwAhgbFWAAxlkAGIBxFgDG8LFRAQZgnAWAARhnAWAA"
                    + "xroBjIsLYOwDgBHAAAzAAIyhDTAA4ywAjOFjowIMwDgLAAMwzgLAAIx1AxiAcRYA"
                    + "BmAEMAXDx6/PuhwS182+zsOvNYAegAEYgx5gAAZgAAZgBDAGPcAAjAdgAAZgAAZg"
                    + "PAADMB6AARiAARiAMegBBmAABmAARgBj0LvwDTn7GmAABmAABmA8AAMwHoABGIAB"
                    + "GIAx6AEGYAAGYAAGYAAGYAAGYOxrgAEYgAEYgPEADMB4AAZgAAZgAMYDMABjXwMM"
                    + "wAAMwAAMwACMfQ0wAAMwAAMwBjDAAIwHYAAGYABGkiQJYCRJkgBGkiQBjCRJEsBI"
                    + "kiQBjCRJAhhJkiSAkSRJAhhJkgQwkiRJACNJkgQwkiQJYCRJkgBGkiQJYCRJEsBI"
                    + "kiQBjCRJEsBIkiQBjCRJAhhJkiSAkSRJAhhJkgQwkiRJACNJkgQwkiQJYCRJkgBG"
                    + "kiQJYCRJEsBIkiQBjCRJEsBIkiSAkSRJAhhJkiSAkSRJACNJkgQwkiRJACNJkgQw"
                    + "kiQJYCRJkgBGkiQJYCRJEsBIkiQBjCRJEsBIkiSAkSRJAhhJkiSAkSRJACNJkgQw"
                    + "kiRJACNJkgBGkiQJYCRJkgBGkiQJYCRJEsBIkiQBjCRJEsBIkiSAkSRJAhhJkiSA"
                    + "kSRJACNJkgQwkiRJACNJkgBGkiQJYCRJkgBGkiQ19gc5Yy3QTgKcGwAAAABJRU5E"
                    + "rkJggg==";

    private static String ENCODED_IMAGE2 =
            "iVBORw0KGgoAAAANSUhEUgAAAJIAAACTCAYAAABlJ0ArAAAKyGlDQ1BJQ0MgUHJv"
                    + "ZmlsZQAASImVlwdUU+kSgP97b3qhBSIgJfTeO0gJoQVQkA6iEpJAQokxIajYUFlc"
                    + "wbWgIgLqCi6KKLgqRdaCWLAtikqxbpBFRV0XCzZU3gUeYfe98947b3LmzJe588/M"
                    + "f8//nzMXACqBIxZnwSoAZItyJFHB/oyExCQGfgBggC4gAwRocLhSMTMyMhygMmX/"
                    + "Lu97ADRub9uM5/r35/9VVHl8KRcAKBLlVJ6Um43yCVSHuWJJDgDIftRvtCRHPM6X"
                    + "UVaXoA2i/GCc0yd5eJxTJxiDmYiJiWKhrAkAgcLhSNIBoBijfkYuNx3NQwlA2V7E"
                    + "E4pQRv8DH66Aw0MZrQuss7MXjbMcZfPUv+RJ/1vOVEVODiddwZN7mRBCgFAqzuIs"
                    + "+z9fx/+W7CzZVA1TVCkCSUgUatG+oL7MRWEKFqXOiZhiIW8ifoIFspDYKeZKWUlT"
                    + "zOMEhCnWZs0Jn+I0YRBbkSeHHTPFfGlg9BRLFkUpaqVJWMwp5kim68oyYxV+AZ+t"
                    + "yJ8niImf4lxh3JwplmZGh03HsBR+iSxK0T9fFOw/XTdIsfds6V/2K2Qr1uYIYkIU"
                    + "e+dM988XMadzShMUvfH4AYHTMbGKeHGOv6KWOCtSEc/PClb4pbnRirU56IGcXhup"
                    + "eIcZnNDIKQYBIBCEoz8GiAWOwA04AA/Ux8rhLx0/o4C1SLxMIkwX5DCY6C3jM9gi"
                    + "rq01w9HewQOA8Ts7eSTe9k3cRYhOmPaJ6QB4oGceqZ72pWoD0IyeIy3itM/4AADK"
                    + "CQA05XNlktxJ3/h1AlhAAspAHWgBPWAEzIEN2p8r8AJ+aMehIALEgESwAHCBAGQD"
                    + "CVgCVoA1oBAUgy1gBygHe0E1OAiOgGOgGZwC58AlcA3cBN3gPpCDQfACDIP3YBSC"
                    + "IDxEhWiQFqQPmUBWkCPkDvlAgVA4FAUlQilQOiSCZNAKaB1UDJVA5dA+qBb6GToJ"
                    + "nYOuQF3QXagfGoLeQJ9hBKbA6rAubArbwe4wEw6DY+D5cDq8GM6DC+BNcBlcBR+G"
                    + "m+Bz8DW4G5bDL+ARBCBkhI4YIDaIO8JCIpAkJA2RIKuQIqQUqULqkVakA7mNyJGX"
                    + "yCcMDkPDMDA2GC9MCCYWw8UsxqzCbMSUYw5imjAXMLcx/ZhhzDcsFauDtcJ6YtnY"
                    + "BGw6dgm2EFuKrcE2Yi9iu7GD2Pc4HI6OM8O54UJwibgM3HLcRtxuXAOuDdeFG8CN"
                    + "4PF4LbwV3hsfgefgc/CF+F34w/iz+Fv4QfxHApmgT3AkBBGSCCLCWkIp4RDhDOEW"
                    + "4SlhlKhCNCF6EiOIPOIy4mbifmIr8QZxkDhKUiWZkbxJMaQM0hpSGamedJH0gPSW"
                    + "TCYbkj3Ic8lCcj65jHyUfJncT/5EUaNYUliUZIqMsolygNJGuUt5S6VSTal+1CRq"
                    + "DnUTtZZ6nvqI+lGJpmSrxFbiKa1WqlBqUrql9EqZqGyizFReoJynXKp8XPmG8ksV"
                    + "ooqpCkuFo7JKpULlpEqvyogqTdVBNUI1W3Wj6iHVK6rP1PBqpmqBajy1ArVqtfNq"
                    + "AzSEZkRj0bi0dbT9tIu0QXWcupk6Wz1DvVj9iHqn+rCGmoazRpzGUo0KjdMacjpC"
                    + "N6Wz6Vn0zfRj9B765xm6M5gz+DM2zKifcWvGB82Zmn6afM0izQbNbs3PWgytQK1M"
                    + "ra1azVoPtTHaltpztZdo79G+qP1ypvpMr5ncmUUzj828pwPrWOpE6SzXqda5rjOi"
                    + "q6cbrCvW3aV7XvelHl3PTy9Db7veGb0hfZq+j75Qf7v+Wf3nDA0Gk5HFKGNcYAwb"
                    + "6BiEGMgM9hl0GowamhnGGq41bDB8aEQycjdKM9pu1G40bKxvPNt4hXGd8T0Toom7"
                    + "icBkp0mHyQdTM9N40/WmzabPzDTN2GZ5ZnVmD8yp5r7mi82rzO9Y4CzcLTItdlvc"
                    + "tIQtXSwFlhWWN6xgK1crodVuqy5rrLWHtci6yrrXhmLDtMm1qbPpt6XbhtuutW22"
                    + "fWVnbJdkt9Wuw+6bvYt9lv1++/sOag6hDmsdWh3eOFo6ch0rHO84UZ2CnFY7tTi9"
                    + "drZy5jvvce5zobnMdlnv0u7y1dXNVeJa7zrkZuyW4lbp1uuu7h7pvtH9sgfWw99j"
                    + "tccpj0+erp45nsc8//Sy8cr0OuT1bJbZLP6s/bMGvA29Od77vOU+DJ8Unx995L4G"
                    + "vhzfKt/HfkZ+PL8av6dMC2YG8zDzlb+9v8S/0f8Dy5O1ktUWgAQEBxQFdAaqBcYG"
                    + "lgc+CjIMSg+qCxoOdgleHtwWgg0JC9ka0svWZXPZtezhULfQlaEXwihh0WHlYY/D"
                    + "LcMl4a2z4dmhs7fNfjDHZI5oTnMEiGBHbIt4GGkWuTjyl7m4uZFzK+Y+iXKIWhHV"
                    + "EU2LXhh9KPp9jH/M5pj7seaxstj2OOW45LjauA/xAfEl8fIEu4SVCdcStROFiS1J"
                    + "+KS4pJqkkXmB83bMG0x2SS5M7plvNn/p/CsLtBdkLTi9UHkhZ+HxFGxKfMqhlC+c"
                    + "CE4VZySVnVqZOsxlcXdyX/D8eNt5Q3xvfgn/aZp3Wknas3Tv9G3pQwJfQangpZAl"
                    + "LBe+zgjJ2JvxITMi80DmWFZ8VkM2ITsl+6RITZQpurBIb9HSRV1iK3GhWL7Yc/GO"
                    + "xcOSMEmNFJLOl7bkqKPD0XWZuew7WX+uT25F7sclcUuOL1VdKlp6fZnlsg3LnuYF"
                    + "5f20HLOcu7x9hcGKNSv6VzJX7lsFrUpd1b7aaHXB6sH84PyDa0hrMtf8utZ+bcna"
                    + "d+vi17UW6BbkFwx8F/xdXaFSoaSwd73X+r3fY74Xft+5wWnDrg3finhFV4vti0uL"
                    + "v2zkbrz6g8MPZT+MbUrb1LnZdfOeLbgtoi09W323HixRLckrGdg2e1vTdsb2ou3v"
                    + "dizccaXUuXTvTtJO2U55WXhZyy7jXVt2fSkXlHdX+Fc0VOpUbqj8sJu3+9Yevz31"
                    + "e3X3Fu/9/KPwx759wfuaqkyrSqtx1bnVT/bH7e/4yf2n2hrtmuKarwdEB+QHow5e"
                    + "qHWrrT2kc2hzHVwnqxs6nHz45pGAIy31NvX7GugNxUfBUdnR5z+n/NxzLOxY+3H3"
                    + "4/UnTE5UNtIai5qgpmVNw82CZnlLYkvXydCT7a1erY2/2P5y4JTBqYrTGqc3nyGd"
                    + "KTgzdjbv7EibuO3lufRzA+0L2++fTzh/58LcC50Xwy5evhR06XwHs+PsZe/Lp654"
                    + "Xjl51f1q8zXXa03XXa43/urya2Ona2fTDbcbLTc9brZ2zeo6c8v31rnbAbcv3WHf"
                    + "udY9p7urJ7anrze5V97H63t2N+vu63u590bv5z/APih6qPKw9JHOo6rfLH5rkLvK"
                    + "T/cH9F9/HP34/gB34MXv0t+/DBY8oT4pfar/tPaZ47NTQ0FDN5/Pez74Qvxi9GXh"
                    + "H6p/VL4yf3XiT78/rw8nDA++lrwee7PxrdbbA++c37WPRI48ep/9fvRD0Uetjwc/"
                    + "uX/q+Bz/+enoki/4L2VfLb62fgv79mAse2xMzJFwJkYBBFU4LQ2AN+icQE0EgHYT"
                    + "ANK8yZl6QqDJ74AJAv+JJ+fuCXEFoLoNgJh8AMJRuwu1pqgq+wEQiWqMH4CdnBT6"
                    + "T5GmOTlO5iI3o6NJ6djYW3R+xFsA8LV3bGy0eWzsaw3a7D0A2t5PzvLjonIY/Xqp"
                    + "dHJyju/LouaDf5F/ALh6ENdDMf1eAAABnWlUWHRYTUw6Y29tLmFkb2JlLnhtcAAA"
                    + "AAAAPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0i"
                    + "WE1QIENvcmUgNS40LjAiPgogICA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93"
                    + "d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPgogICAgICA8cmRm"
                    + "OkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIgogICAgICAgICAgICB4bWxuczpleGlm"
                    + "PSJodHRwOi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyI+CiAgICAgICAgIDxleGlm"
                    + "OlBpeGVsWERpbWVuc2lvbj4xNDY8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAg"
                    + "ICAgICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MTQ3PC9leGlmOlBpeGVsWURpbWVu"
                    + "c2lvbj4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgIDwvcmRmOlJERj4KPC94"
                    + "OnhtcG1ldGE+Co2UevIAAAe5SURBVHgB7Zy/SnNNEMY3HyK+pYVgSgtB0kYsLHID"
                    + "9kIuQK2sFaxzBam8CbsUuQIrLWNhYyUB7cUY+L6cFwIx387szGbOObvJExDN2dn5"
                    + "85vnfXdHgo1/Zy+HFwisSOCfFfdjOwj8JQAhQQgmBCAkE4xwAiFBAyYEtigvDw8P"
                    + "bjgcUstJPr+/v/fm1Wg0vM/nD33zRmjPfK/muy9Osf/q6krjpnbb4+Njd3Fx8TuP"
                    + "YmrzvW5uboppLqsvXx3Fs1Advn2hPTHrvjiS/GJilbnn/Pz8f6XgaJsRx2t1AhDS"
                    + "6gzhYUYAQoIMTAhASCYY4YSc2ig0W1tb7vDwkFqu5Pnr66ubTqeVxKozyNHRkStj"
                    + "epTW9Pb25r6+vkTmaiHt7e250Wgkcl6WUbPZdOPxWOx+NmKIbVc1tIz1/Pzs/vz5"
                    + "s2pK0ftPT0/d4+OjaD+ONhEmGIUIQEghQlgXEYCQRJhgFCIAIYUIYV1EAEISYYJR"
                    + "iIB6auMc3t3duaenJ85EvHZ2duaur6/F9pxhnSM0l1fsWr/fd4PBIHb7r33tdtv1"
                    + "er1fz2LemAqpEJHVJwYODg5i6jHdQ43ydQuz+PWLFWcrYDjarEhuuB8IacMFYFU+"
                    + "hGRFcsP9QEgbLgCr8k0v21ZJ5eyHu4hTl/ec653nDiHNSSi+U4LgRKRwn6UpjrYs"
                    + "25Ze0hBSej3JMiMIKcu2pZc0hJReT7LMCELKsm3pJY2pLaInmzydUbggpBkZapyn"
                    + "oHHPLX1xcVJbw9GWWkcyzQdCyrRxqaUNIaXWkUzzgZAybVxqaZtetouPx1p9srHT"
                    + "6VTGynIK43xZXcQt2bRaLRPOpkKy+oy1SWULTrgGco1fcJHUj91u1xVfKb1wtKXU"
                    + "jYxzgZAybl5KqUNIKXUj41wgpIybl1LqEFJK3cg4F/XUVvxdop2dnVpLnkwmqvg5"
                    + "TmZFgbu7u6o6rY1/fn7ELtVCKkbp7+9vcYAcDH2/HgiJL2aPlkVOnHG0absLey8B"
                    + "CMmLBQ+1BCAkLTHYewlASF4seKgl0JhdGqv7k6/a7IzsU7g4F6WsM2r11GbU20rd"
                    + "xDSQ2xMSZqXFJRIMR1sijcg9DQgp9w4mkj+ElEgjck8DQsq9g4nkT162q7xQchdb"
                    + "HycuN58vzt7nv6xnVB6+nLkcKD/cHm6Nis/FWd6D/5E4wlgTE4CQxKhgyBGAkDg6"
                    + "WBMTgJDEqGDIEYCQODpYExOAkMSoYMgRIMd/blPKa9zImnLedeZmwYwU0vLvCSSF"
                    + "WiQkiUPlxsW33EPlyMUv9lA5UP5yeo6jLaduJZwrhJRwc3JKDULKqVsJ5wohJdyc"
                    + "nFIjL9vcxdH60sjFsoIZEyNmT535WsWO8UMKKcZZjMDqbFZMjaE9MQxCPjXrVHyO"
                    + "M7VHExdHm4YWbEkCEBKJBgsaAhCShhZsSQIQEokGCxoCEJKGFmxJAlFTGzcBkJGw"
                    + "4CVgzTLGH7dHOtGRQqIccEG9pAQPqViCrSubxNZTRc7aGKFafP5Ce6SAcbRJScGO"
                    + "JQAhsXiwKCUAIUlJwY4lACGxeLAoJUBetqUOyrKzugTO8/NdNOdrVt+tc+b8xdTD"
                    + "+VuVgbmQfAWWWcCqAHz5Fj5TzpmqmaolVA+3j4q1/BxH2zIRvI8iACFFYcOmZQIQ"
                    + "0jIRvI8iACFFYcOmZQIQ0jIRvI8iYD61RWVRwSZuCouZWjh/FZRDTpUxtVD5cjUu"
                    + "x1ELadkBlcTi86r2LMZc/JkDsmi3+DOXc4y/Rd/Sn6kcYuJTvqS5hOxwtIUIYV1E"
                    + "AEISYYJRiACEFCKEdREBCEmECUYhAurLdsgh1tMkwF3QLS7i5P9It7e3f0fMIoFc"
                    + "vqgWFqC4L2pfqs+rqkUThxRSqhCRV5oEIKQ0+5JdVhBSdi1LM2EIKc2+ZJeVemor"
                    + "Lt7b29u1FjqZTMz+sGfZ00wZoKrKWRNHLaT9/X33/v5eBh+xz2az6cbjsdje2rCY"
                    + "ZrQvrilaXzH2MTlr4uBo09CCLUkAQiLRYEFDAELS0IItSQBCItFgQUMAQtLQgi1J"
                    + "QD21kZ5mC/1+341GI85EvNbpdFy32xXbl2HITVplT0Fl1FOmT1MhDQYDNxwOzfKt"
                    + "W0hmhQQcaUWptQ+EJ5c1cXC0kRixoCEAIWlowZYkACGRaLCgIQAhaWjBliRgetkm"
                    + "o9S8wE1fsalRPjUXVC425X++xxcnZs/c36rfN0JIsZB8zSp8hRrmi0f58tnm+AxH"
                    + "W45dSzBnCCnBpuSYEoSUY9cSzBlCSrApOaYEIeXYtQRzxtRm3JSYiS4mhariSHOD"
                    + "kGak1mk0r6sWHG3Sf3KwYwlASCweLEoJQEhSUrBjCUBILB4sSgmYXrbb7bY0btCu"
                    + "1WoFbco0SG0qWrXW2Hqkl3dTIfV6vVXrXev9vqZwDfbZF4C4PXUBxNFWF/k1iwsh"
                    + "rVlD6yoHQqqL/JrFhZDWrKF1lQMh1UV+zeKqp7aPjw9X92j++flp2oaqpiBtHK29"
                    + "KRSlM7WQptOpe3l5UYbZHPOYkd23p0oR+eJrO4ajTUsM9l4CEJIXCx5qCUBIWmKw"
                    + "9xKAkLxY8FBLgLxsn5ycuMvLS60/2G8ogcbsxq7/W78bCgtl0wRwtNFssKIgACEp"
                    + "YMGUJgAh0WywoiAAISlgwZQmACHRbLCiIPAfW6CPN1CnbpEAAAAASUVORK5CYII=";

    private static String AUTOSTART_TOKEN = "2f5cdf44-8e71-4787-bde1-31817277accf";
    private static String DECODED_IMAGE = "bankid:///?autostarttoken=" + AUTOSTART_TOKEN;

    @Test
    public void testQrCodeParser() throws IOException {
        final String decoded = QrCodeParser.decodeQRCode(ENCODED_IMAGE);
        assertThat(decoded).isEqualTo(DECODED_IMAGE);
    }

    @Test
    public void testBankIdQrCodeParser() throws NoSuchElementException {
        final String token = QrCodeParser.decodeBankIdQrCode(ENCODED_IMAGE);
        assertThat(token).isEqualTo(AUTOSTART_TOKEN);
    }

    @Test(expected = IOException.class)
    public void testQrCodeParserFailure() throws IOException {
        QrCodeParser.decodeQRCode("");
    }

    @Test(expected = NoSuchElementException.class)
    public void testBankIdQrCodeParserFailure() throws IOException {
        QrCodeParser.decodeBankIdQrCode(ENCODED_IMAGE2);
    }
}
