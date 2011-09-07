#!/usr/bin/env python

import glob
import os
import shutil
import sys
import subprocess
import zipfile

_ROOT = os.path.abspath(os.path.dirname(__file__))
DOC = os.path.join(_ROOT, 'doc')
EXAMPLE = os.path.join(DOC, 'example')
TARGET = os.path.join(_ROOT, 'target')
LIB = os.path.join(EXAMPLE, 'lib')
TESTS = os.path.join(EXAMPLE, 'robot-tests')


def main():
    _copy_libraries()
    _run_tests()
    _zip_example()

def _copy_libraries():
    jars = glob.glob(os.path.join(TARGET, 'remoteapplications-*-with-dependencies.jar'))
    shutil.copy(sorted(jars)[-1], LIB)

def _run_tests():
    rc = _run()
    if rc != 0:
        print "Failed to run the tests."
        sys.exit(1)

def _run():
    runner = os.path.join(EXAMPLE, 'run.py')
    return subprocess.call(['python', runner, TESTS],
                           shell=os.name=='nt')

def _zip_example():
    zip_target_path = os.path.join(TARGET, 'remoteapplications_example.zip')
    zip = zipfile.ZipFile(zip_target_path, 'w')
    paths = _get_paths([(LIB, '*.jar'), (TESTS, '*.*'), (EXAMPLE, '*.*')])
    print 'Zipping files...'
    for path in paths:
    	path_in_zip_file = path.replace(DOC, '')
    	print '  %s' % (path_in_zip_file)
        zip.write(path, path_in_zip_file)
    print "Created zip file '%s'." % (zip_target_path)

def _get_paths(patterns):
    paths = []
    for dir, pattern in patterns:
        paths.extend(glob.glob(os.path.join(dir, pattern)))
    return paths


if __name__ == '__main__':
    main()
