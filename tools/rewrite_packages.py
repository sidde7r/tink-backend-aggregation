#!/usr/bin/env python3
import os
import glob
import sys

REWRITTEN_CLASSES = {}


def fix_package(relpath, dirpath, filename):
    global REWRITTEN_CLASSES
    package_line_num = None
    if filename.endswith(".java"):

        with open(relpath, 'r') as f:
            file_lines = f.readlines()
            for ii in range(0, len(file_lines)):
                if file_lines[ii].startswith("package ") and file_lines[ii].endswith(";\n"):
                    package_line_num = ii
                    package_line = file_lines[ii]
                    break

        if package_line_num is None:
            print("  package line not found: " + relpath)
            return
        # everything after 'package ' and before ';'
        file_package = package_line[8:package_line.find(";")]

        class_name = filename[:-5]  # strip ".java"
        layout_package = dirpath.replace("/", ".").replace("..", ".")
        if layout_package.startswith("."):
            layout_package = layout_package[1:]  # strip leading .

        if not file_package == layout_package:
            old_class = file_package + "." + class_name
            new_class = layout_package + "." + class_name
            print("  " + old_class + " -> " + new_class)
            REWRITTEN_CLASSES[old_class] = new_class

            with open(relpath, 'w') as w:
                file_lines[package_line_num] = "package " + \
                    layout_package + ";\n"
                w.writelines(file_lines)


def rewrite_imports(p):
    global REWRITTEN_CLASSES
    rewritten_count = 0
    with open(p, 'r') as f:
        file_lines = f.readlines()

    for ii in range(0, len(file_lines)):
        l = file_lines[ii]
        if l.startswith("import static "):
            import_class = l[14:l.rfind(".")]
            import_method = l[l.rfind(".")+1:l.rfind(";")]
            if import_class in REWRITTEN_CLASSES:
                file_lines[ii] = "import static " + \
                    REWRITTEN_CLASSES[import_class] + \
                    "." + import_method + ";\n"
                rewritten_count += 1
        elif l.startswith("import "):
            import_class = l[7:l.find(";")]
            if import_class in REWRITTEN_CLASSES:
                file_lines[ii] = "import " + \
                    REWRITTEN_CLASSES[import_class] + ";\n"
                rewritten_count += 1
    if rewritten_count > 0:
        with open(p, 'w') as f:
            f.writelines(file_lines)
        print("  " + p + " rewrote " + str(rewritten_count) + " imports")


def walk(absroot, dirfilter):
    print("rewriting packages in main")
    basepath = os.path.join(absroot, "src", "main", "java")
    os.chdir(basepath)
    for dirpath, dnames, fnames in os.walk("./"):
        if dirfilter in dirpath:
            print("  processing files in " + dirpath)
            for f in fnames:
                if f.endswith(".java"):
                    fix_package(os.path.join(dirpath, f), dirpath, f)
        else:
            for f in fnames:
                pass

    print("rewriting packages in tests")
    basepath = os.path.join(absroot, "src", "test", "java")
    os.chdir(basepath)
    for dirpath, dnames, fnames in os.walk("./"):
        if dirfilter in dirpath:
            print("  processing files in " + dirpath)
            for f in fnames:
                if f.endswith(".java"):
                    fix_package(os.path.join(dirpath, f), dirpath, f)
        else:
            for f in fnames:
                pass

    print("rewriting imports in main")
    os.chdir(os.path.join(absroot, "src", "main", "java"))
    for dirpath, dnames, fnames in os.walk("./"):
        for f in fnames:
            if f.endswith(".java"):
                rewrite_imports(os.path.join(dirpath, f))

    print("rewriting imports in tests")
    os.chdir(os.path.join(absroot, "src", "test", "java"))
    for dirpath, dnames, fnames in os.walk("./"):
        for f in fnames:
            if f.endswith(".java"):
                rewrite_imports(os.path.join(dirpath, f))


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("usage:")
        print("    rewrite_packages.py [path] [filter]")
        print("")
        print("'path' should point to the top level of a maven-style project layout - i.e. it should contain ")
        print("       'src/main/java' and 'src/test/java' subdirectories containing code and tests")
        print("")
        print("'filter' is a string that must appear in the package name for the file's package to be rewritten")
        print("filter applies only to package name rewrites - import rewrites for the re-written packages are")
        print("applied to the entire contents of 'path'")
        print("")
        print("This script assumes that the files are already AOSP-formatted, and may produce incorrect output if not")
        print("")

    absroot = os.path.abspath(sys.argv[1])
    if os.path.isdir(os.path.join(absroot, "src", "main", "java")) and os.path.isdir(os.path.join(absroot, "src", "test", "java")):
        walk(absroot, sys.argv[2])
    else:
        print("not continuing, maven folder structure not found")
