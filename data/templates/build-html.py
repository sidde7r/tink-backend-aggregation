#!/usr/bin/env python
import sys

if len(sys.argv) != 3:
	print "Usage: ./built-html.py <head-file> <body-file>"
	sys.exit(1)

with file('html-activity-format.html') as f:
  template = f.read()

with file('default-style.html') as f:
  default_style = f.read()

with file(sys.argv[1]) as f:
  head = f.read()

with file(sys.argv[2]) as f:
  body = f.read()

s=template.format(default_style, head, body)
with file('test-activity.html', 'w') as f:
  f.write(s)
