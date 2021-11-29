#!/usr/bin/env python3
# tink:doctest_ignore
import unittest
from io import BytesIO
from unittest.mock import MagicMock, call, patch
from zipfile import ZipFile

import compile_srcjar


def make_zip_file(files):
    zipfile = MagicMock()
    zipfile.__enter__.return_value = zipfile
    zipfile.namelist.return_value = files.keys()
    zipfile.read.side_effect = files.__getitem__
    return zipfile


def make_zip_files(description):
    return {name: make_zip_file(files) for name, files in description.items()}


class FileBag:
    def __init__(self):
        self.files = {}

    def __call__(self, fn, *_al, **_kw):
        return self.files.setdefault(fn, BytesIO(b"DEFAULT FILE-MEAT"))


class ZipBag:
    def __init__(self):
        self.files = make_zip_files(
            {
                "a.srcjar": {"inner.java": b"a-meat"},
                "b.srcjar": {"inner.java": b"b-meat"},
                "c.srcjar": {"inner.java": b"b-meat"},
            }
        )

    def __call__(self, fn, *_al, **_kw):
        def mk_zip():
            z = MagicMock(wraps=ZipFile(BytesIO(), "w"))
            z.__enter__.return_value = z
            return z

        return self.files.setdefault(fn, mk_zip())


def p(n, new_callable):
    return patch(f"compile_srcjar.{n}", new_callable=new_callable, create=True)


@p("print", MagicMock)
@p("ZipFile", lambda: MagicMock(side_effect=ZipBag()))
@p("open", lambda: MagicMock(side_effect=FileBag()))
class TestOldMain(unittest.TestCase):
    def assert_zip_output(self, zipfile_m, zip_name, files_written):
        zipfile_m.assert_any_call(zip_name, "w", compression=0)
        zf = zipfile_m.side_effect.files[zip_name]
        zil = zf.infolist()

        self.assertEqual(sorted(zil, key=lambda i: i.date_time), zil)

        for zi in zil:
            self.assertEqual((1980, 1, 1, 0, 0, 0), zi.date_time)

        self.assertEqual(
            [name for name, _ in files_written],
            [zi.filename for zi in zil],
        )

        for name, meat in files_written:
            with self.subTest(name):
                self.assertEqual(meat, zf.read(name))

    def test_empty(self, open_m, zipfile_m, print_m):
        rc = compile_srcjar.main(["it", "outfile"])

        open_m.assert_not_called()
        zipfile_m.assert_not_called()
        print_m.assert_called_once_with(
            "usage: it <output.srcjar> (<in.java>|<in.srcjar>)..."
        )
        self.assertEqual(1, rc or 0)

    def test_simple(self, open_m, zipfile_m, print_m):
        open_m.side_effect.files = {"one.java": BytesIO(b"ONE")}

        rc = compile_srcjar.main(["it", "outfile", "one.java"])

        open_m.assert_called_once_with("one.java", "rb")
        self.assert_zip_output(zipfile_m, "outfile", (("one.java", b"ONE"),))
        print_m.assert_not_called()
        self.assertEqual(0, rc or 0)

    def test_junk(self, open_m, zipfile_m, print_m):
        rc = compile_srcjar.main(["zonk", "outfile", "README.md"])

        open_m.assert_not_called()
        zipfile_m.assert_not_called()
        print_m.assert_called_once_with(
            "usage: zonk <output.srcjar> (<in.java>|<in.srcjar>)..."
        )
        self.assertEqual(1, rc or 0)

    def test_srcjar(self, _open_m, zipfile_m, print_m):
        rc = compile_srcjar.main(["it", "o", "a.srcjar"])

        expected = [call("a.srcjar", "r"), call("o", "w", compression=0)]
        zipfile_m.assert_has_calls(expected)
        self.assert_zip_output(zipfile_m, "o", (("inner.java", b"a-meat"),))
        print_m.assert_not_called()
        self.assertEqual(0, rc or 0)

    def test_srcjar_nonconflict(self, _open_m, zipfile_m, print_m):
        rc = compile_srcjar.main(["it", "o", "b.srcjar", "c.srcjar"])

        expected = [
            call("b.srcjar", "r"),
            call("c.srcjar", "r"),
            call("o", "w", compression=0),
        ]
        zipfile_m.assert_has_calls(expected)
        self.assert_zip_output(zipfile_m, "o", (("inner.java", b"b-meat"),))
        print_m.assert_not_called()
        self.assertEqual(0, rc or 0)

    def test_srcjar_conflict(self, _open_m, zipfile_m, print_m):
        rc = compile_srcjar.main(["it", "o", "a.srcjar", "b.srcjar"])

        self.assertEqual(1, rc)
        expected = [call("a.srcjar", "r"), call("b.srcjar", "r")]
        self.assertEqual(expected, zipfile_m.call_args_list)
        print_m.assert_called_once_with(
            "Conflicting versions of inner.java found."
        )

    def test_srcjar_plain_conflict(self, open_m, zipfile_m, print_m):
        open_m.side_effect.files = {"inner.java": BytesIO(b"SOMETHING")}

        rc = compile_srcjar.main(["i", "o", "a.srcjar", "inner.java"])

        zipfile_m.assert_called_once_with("a.srcjar", "r")
        print_m.assert_called_once_with(
            "Conflicting versions of inner.java found."
        )
        self.assertEqual(1, rc)


if __name__ == "__main__":
    unittest.main()
