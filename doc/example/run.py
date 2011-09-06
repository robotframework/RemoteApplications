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
    rc = subprocess.call(['jybot', '--outputdir', outputdir] + args,
                         shell=os.name=='nt')
    sys.exit(rc)

if __name__ == '__main__':
    main(sys.argv[1:])
