#!/usr/bin/env python

import os
import sys
import subprocess
from glob import glob

def main(args):
    dir = os.path.dirname(__file__)
    lib = os.path.join(dir, 'lib')
    jars = glob(os.path.join(lib, '*.jar'))
    os.environ['CLASSPATH'] = os.pathsep.join(jars)
    outputdir = os.path.join(dir, 'results')
    return subprocess.call(['jybot', '--outputdir', outputdir] + args,
                            shell=os.name=='nt')

if __name__ == '__main__':
    args = sys.argv[1:]
    if not args:
        print "Usage: run.py [robot options] datasources"
        sys.exit(1)
    sys.exit(main(args))
