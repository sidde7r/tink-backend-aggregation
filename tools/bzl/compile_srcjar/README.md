# compile_srcjar

Copies, into an output srcjar, free-floating java source files and sources
from other srcjars. Note that it filters the input filename list, but not
the contents of srcjars.

## usage
    compile_srcjar.py <output.srcjar> (<in.java>|<in.srcjar>)...
