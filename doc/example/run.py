#!/usr/bin/env python

import os
import sys
from glob import glob

def main(args):
    dir = os.path.dirname(__file__)
    lib = os.path.join(dir, 'lib')
    jars = glob(os.path.join(lib, '*.jar'))
    os.environ['CLASSPATH'] = os.pathsep.join(jars)
    os.environ['PYTHONPATH'] = lib
    outputdir = os.path.join(dir, 'results')
    rc = os.system('jybot --loglevel TRACE --outputdir "%s" %s' % (outputdir, ' '.join(args))) >> 8
    sys.exit(rc)

if __name__ == '__main__':
    main(sys.argv[1:])
