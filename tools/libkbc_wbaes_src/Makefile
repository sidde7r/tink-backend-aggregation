PROJECT=kbc_wbaes
SOURCES=kbc_wbaes.c gf2matrix.c m4ri_gf2matrix_impl.c wb_aes.c aes.c
LIBRARY=shared
INCPATHS=
LIBPATHS=
LDFLAGS=-lm4ri
CFLAGS=-c -Wall -std=c11
CC=gcc

OBJECTS=$(SOURCES:.c=.o)
INCFLAGS=$(foreach TMP,$(INCPATHS),-I$(TMP))
LIBFLAGS=$(foreach TMP,$(LIBPATHS),-L$(TMP))

ifeq "$(LIBRARY)" "shared"
	BINARY=lib$(PROJECT).so
	LDFLAGS += -shared
else ifeq "$(LIBRARY)" "static"
	BINARY=lib$(PROJECT).a
else
	BINARY=$(PROJECT)
endif

all: $(SOURCES) $(BINARY)

$(BINARY): $(OBJECTS)
ifeq "$(LIBRARY)" "static"
	ar rcs $(BINARY) $(OBJECTS)
else
	$(CC) $(LIBFLAGS) $(OBJECTS) $(LDFLAGS) -o $@
endif

.c.o:
		$(CC) $(INCFLAGS) $(CFLAGS) -fPIC $< -o $@

distclean: clean
		rm -f $(BINARY)

clean:
		rm -f $(OBJECTS)

