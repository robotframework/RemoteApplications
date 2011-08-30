#!/usr/bin/env python

import os

if __name__ == '__main__':
    os.environ['CLASSPATH'] = ''
    jar = os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'test', 
                       'resources', 'test-app', 'test-application.jar')
    os.popen2('java -jar %s' % jar)