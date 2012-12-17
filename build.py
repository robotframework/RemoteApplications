#!/usr/bin/env python

import sys
import os
import subprocess
import inspect
import glob
import zipfile
import shutil
from tempfile import gettempdir


def _exists(path):
    return os.path.exists(path)


def _join(*paths):
    return os.path.join(*paths)


def _read_version():
    for line in open('pom.xml').readlines():
        line = line.strip()
        if line.startswith('<version>'):
            return line[9:-10]

ROOTDIR = os.path.dirname(os.path.abspath(__file__))
TARGET = _join(ROOTDIR, 'target')
SRC = _join(ROOTDIR, 'src')
VERSION = _read_version()
DIST_JAR = _join(TARGET,
                 'remoteapplications-%s-jar-with-dependencies.jar' %
                 VERSION)
FINAL_JAR = _join(TARGET, 'remoteapplications-%s.jar' % VERSION)
ROBOT_INSTALLATION = [p for p in sys.path if
                      os.path.exists(os.path.join(p, 'robot'))][0]
TEST_LIB = _join(TARGET, 'test-classes', 'test-lib')
REMOTE_LIB_IN_TEST_LIB = TEST_LIB = _join(TEST_LIB, 'remoteapplications.jar')

class _Task(object):

    def _shell(self, cmd):
        print 'Running %s' % ' '.join(cmd)
        windows = os.name == 'nt'
        return subprocess.call(cmd, shell=windows)

    def __str__(self):
        return self.__doc__


class Package(_Task):
    """Package RemoteApplications with dependencies in a jar file"""

    def execute(self):
        self._maven()
        self._unit_tests()
        self._jar_jar()
        self._copy_to_target()

    def _maven(self):
        self._shell(['mvn', 'clean', 'package', 'assembly:single'])

    def _unit_tests(self):
        os.environ['CLASSPATH'] = DIST_JAR
        self._shell(['jython', _join(SRC, 'test', 'python',
                                     'test_remote_applications.py')])

    def _jar_jar(self):
        mf_path = self._extract_manifest()
        self._run_jarjar()
        tmpdir = os.path.join(TARGET, 'tmp')
        if os.path.exists(tmpdir):
            shutil.rmtree(tmpdir)
        self._unzip_file_into_dir(DIST_JAR, tmpdir)
        self._rmi_compile(tmpdir)
        shutil.copy(os.path.join('src', 'main', 'python',
                                 'RemoteApplications.py'), tmpdir)
        self._rejar(mf_path, tmpdir)
        os.remove(mf_path)

    def _extract_manifest(self):
        zfobj = zipfile.ZipFile(DIST_JAR)
        for name in zfobj.namelist():
            if name == 'META-INF/MANIFEST.MF':
                mf_path = os.path.join(TARGET, 'MANIFEST.MF')
                outfile = open(mf_path, 'wb')
                outfile.write(zfobj.read(name))
                outfile.close()
                return mf_path

    def _run_jarjar(self):
        jarjardir = os.path.join(ROOTDIR, 'lib')
        jarjarjar = os.path.join(jarjardir, 'jarjar-1.0.jar')
        rules = os.path.join(jarjardir, 'rules.txt')
        self._shell(['java', '-jar', jarjarjar, 'process', rules, DIST_JAR,
                DIST_JAR])

    def _unzip_file_into_dir(self, file, dir):
        os.mkdir(dir, 0777)
        zfobj = zipfile.ZipFile(file)
        for name in zfobj.namelist():
            if name.endswith('/'):
                if not os.path.exists(os.path.join(dir, name)):
                    os.makedirs(os.path.join(dir, name))
            else:
                outfile = open(os.path.join(dir, name), 'wb')
                outfile.write(zfobj.read(name))
                outfile.close()

    def _rmi_compile(self, tmpdir):
        class_name = 'org.robotframework.remoteapplications.org.springframework.remoting.rmi.RmiInvocationWrapper'
        self._shell(['rmic', '-verbose', '-classpath', tmpdir, '-d', tmpdir,
                     class_name])
        self._shell(['rmic', '-verbose', '-iiop', '-always', '-classpath',
                     tmpdir, '-d', tmpdir, class_name])

    def _rejar(self, mf_path, dir):
        self._shell(['jar', 'cfm', DIST_JAR, mf_path, '-C', dir, '.'])

    def _copy_to_target(self):
        shutil.copy2(DIST_JAR, REMOTE_LIB_IN_TEST_LIB)


class Test(_Task):
    """Run the acceptance tests"""

    def __init__(self):
        self._deps_file = _join(gettempdir(), 'remoteapps-dependencies.txt')
        self._default_args = ['robot-tests']

    def execute(self):
        self.add_dependencies_to_classpath()
        args = sys.argv[2:] if len(sys.argv) > 2 else self._default_args
        self.run_robot_tests(args)

    def add_dependencies_to_classpath(self):
        if not _exists(self._deps_file):
            self._shell(['mvn', '-DoutputAbsoluteArtifactFilename=true',
                    'dependency:list', '-DoutputFile=%s' % self._deps_file])
        os.environ['CLASSPATH'] = os.pathsep.join([DIST_JAR,
            _join('target', 'test-classes')] + self.get_test_deps())

    def get_test_deps(self):
        deps = open(self._deps_file, 'rb').read().splitlines()
        return [dep.split(':')[-1] for dep in deps
                if 'swinglibrary' in dep or 'mortbay' in dep]

    def run_robot_tests(self, args):
        runner = _join(ROBOT_INSTALLATION, 'robot', 'run.py')
        cmd = ['jython', '-Dpython.path="%s"' % ROBOT_INSTALLATION,
               runner, '--debugfile', 'debug.txt', '--loglevel', 'TRACE',
               '--outputdir', gettempdir()]
        return self._shell(cmd + args)


class Demo(_Task):
    """Package demo as zip file"""

    def __init__(self):
        self._demodir = _join(ROOTDIR, 'demo')
        self._libdir = _join(self._demodir, 'lib')
        self._testdir = _join(self._demodir, 'robot-tests')
        self._demo_zip_path = _join(TARGET, 'remoteapplications-demo.zip')

    def execute(self):
        self._copy_libraries()
        self._run_tests()
        self._zip_example()

    def _copy_libraries(self):
        shutil.copy(DIST_JAR, self._libdir)

    def _run_tests(self):
        rc = self._run()
        if rc != 0:
            print "Failed to run the tests."
            sys.exit(1)

    def _run(self):
        runner = os.path.join(self._demodir, 'run.py')
        return self._shell(['python', runner, self._testdir])

    def _zip_example(self):
        zip = zipfile.ZipFile(self._demo_zip_path, 'w')
        paths = self._get_paths()
        print 'Zipping files...'
        for path in paths:
            zip_path = path.replace(self._demodir, 'remoteapplications-demo')
            print '  %s' % (zip_path)
            zip.write(path, zip_path)
        print "Created zip file '%s'." % (self._demo_zip_path)

    def _get_paths(self):
        paths = []
        for dir, pattern in [(self._libdir, '*.jar'),
                             (self._testdir, '*.*'), (self._demodir, '*.*')]:
            paths.extend(glob.glob(os.path.join(dir, pattern)))
        return paths


class Doc(_Task):
    """Create library documentation with libdoc"""

    def execute(self):
        Test().add_dependencies_to_classpath()
        output = _join(TARGET, 'RemoteApplications-%s.html' % VERSION)
        lib = _join(ROOTDIR, 'src', 'main', 'python', 'RemoteApplications.py')
        command = 'jython -Dpython.path=%s -m robot.libdoc -v %s %s %s' % \
                    (ROBOT_INSTALLATION, VERSION, lib, output)
        self._shell(command.split())


class Dist(_Task):
    """Create package, demo and docs"""

    def execute(self):
        Package().execute()
        Demo().execute
        Doc().execute
        shutil.copy(DIST_JAR, FINAL_JAR)


class Tasks(object):

    def __init__(self):
        self._tasks = self._parse_tasks()

    def get(self, name):
        return self._tasks[name]()

    def _parse_tasks(self):
        def _is_task(item):
            return (inspect.isclass(item) and
                    issubclass(item, _Task) and
                    item is not _Task)
        return dict([(t.__name__.lower(), t) for t in globals().values()
                     if _is_task(t)])

    def __str__(self):
        return '\n'.join(name + '\n\t' + t.__doc__ for (name, t)
                         in self._tasks.items())


if __name__ == '__main__':
    tasks = Tasks()
    try:
        tasks.get(sys.argv[1]).execute()
    except (KeyError, IndexError):
        print "Usage:  build.py task\n\nAvailable tasks:\n%s" % tasks
