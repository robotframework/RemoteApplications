#!/usr/bin/env python

import os
import sys
import subprocess
from  javaws_app.FileServer import FileServer

def main(args):
    os.environ['CLASSPATH'] = os.pathsep.join(['lib/example_application.jar', 'lib/remoteapplications-2.0.jar'])
    server =  FileServer()
    server.start()
    result = subprocess.call(['jybot'] + args, shell=os.name=='nt')
    server.stop()
    return result

if __name__ == '__main__':
    args = sys.argv[1:]
    if not args:
        print "Usage: run.py [robot options] datasources"
        sys.exit(1)
    sys.exit(main(args))
