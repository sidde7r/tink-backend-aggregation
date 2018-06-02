#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""Prints stdin to stdout with UUID-lookalikes replaced with new UUIDs."""
import sys
import uuid
import re

def find_all_uuids(s):
	pattern = re.compile("'[0-9a-f]{32}'")
	return set(pattern.findall(s))
	

def generate_new_uuids(s):
	"""Replace everything that looks like a UUID with a new UUID.

	returns -- the replaced string.
	"""
	pattern = re.compile("'[0-9a-f]{32}'")
	repl = uuid.uuid4().hex
	s = pattern.sub(repl, s)

	pattern = re.compile(repl)
	random = uuid.uuid4()
	s, nchanges = pattern.subn("'{0}'".format(random.hex), s, count=1)
	while nchanges > 0:
		random = uuid.uuid4()
		s, nchanges = pattern.subn("'{0}'".format(random.hex), s, count=1)

	return s


def main():
	s = sys.stdin.read()
	uuids = find_all_uuids(s)
	replacements = [(olduuid, "'{0}'".format(uuid.uuid4().hex)) for olduuid in uuids]
	for needle, new_uuid in replacements:
		s = s.replace(needle, new_uuid)
	print s


if __name__ == '__main__':
	main()
