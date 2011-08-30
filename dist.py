#!/usr/bin/env python

import subprocess

def call(cmd):
    print " ".join(cmd)
    return subprocess.call(cmd)

if __name__ == '__main__':
	call(['mvn', 'clean', 'assembly:assembly'])
	call(['python', 'jarjar.py'])
	call(['python', 'create_example.py'])
	call(['python', 'create_doc.py'])
