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
DIST_JAR = _join(TARGET,
                 'remoteapplications-%s-jar-with-dependencies.jar' %
                 _read_version())


class PackageBuilder(object):

    def package(self):
        self._maven()
        self._unit_tests()
        self._jar_jar()

    def _maven(self):
        _shell(['mvn', 'clean', 'package', 'assembly:single'])

    def _unit_tests(self):
        _shell(['jython', _join(SRC, 'test', 'python', 'test_remote_applications.py')])

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
        _shell(['java', '-jar', jarjarjar, 'process', rules, DIST_JAR,
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
        _shell(['rmic', '-verbose', '-classpath', tmpdir, '-d', tmpdir, class_name])
        _shell(['rmic', '-verbose', '-iiop', '-always', '-classpath', tmpdir, '-d',
              tmpdir, class_name])

    def _rejar(self, mf_path, dir):
        _shell(['jar', 'cfm', DIST_JAR, mf_path, '-C', dir, '.'])


class TestRunner(object):

    def __init__(self):
        self._deps_file = _join(gettempdir(), 'remoteapps-dependencies.txt')
        self._default_args = ['robot-tests']

    def execute(self):
        self.add_dependencies_to_classpath()
        args = sys.argv[2:] if len(sys.argv) > 2 else self._default_args
        self.run_robot_tests(args)

    def add_dependencies_to_classpath(self):
        if not _exists(self._deps_file):
            _shell(['mvn', '-DoutputAbsoluteArtifactFilename=true',
                    'dependency:list', '-DoutputFile=%s' % self._deps_file])
        dependencies = [DIST_JAR, _join('target', 'test-classes')] + self.get_test_deps()
        os.environ['CLASSPATH'] = os.pathsep.join(dependencies)

    def get_test_deps(self):
        deps = open(self._deps_file, 'rb').read().splitlines()
        return [ dep.split(':')[-1] for dep in deps if 'swinglibrary' in dep or 'org/mortbay' in dep ]

    def _robot_installation_path(self):
        for path in sys.path:
            if os.path.exists(os.path.join(path, 'robot')):
                return path

    def run_robot_tests(self, args):
        runner = _join(self._robot_installation_path(), 'robot', 'runner.py')
        command = '''jython
-Dpython.path="%s"
%s
--debugfile
debug.txt
--loglevel
TRACE
--outputdir
%s''' % (self._robot_installation_path(), runner, gettempdir())
        return _shell(command.strip().splitlines() + args)


class DemoPackager(object):

    def __init__(self):
        self._demodir = _join(ROOTDIR, 'demo')
        self._libdir = _join(self._demodir, 'lib')
        self._testdir = _join(self._demodir, 'robot-tests')

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
        return _shell(['python', runner, self._testdir])

    def _zip_example(self):
        zip_target_path = _join(TARGET, 'remoteapplications-demo.zip')
        zip = zipfile.ZipFile(zip_target_path, 'w')
        paths = self._get_paths()
        print 'Zipping files...'
        for path in paths:
            path_in_zip_file = path.replace(self._demodir, 'remoteapplications-demo')
            print '  %s' % (path_in_zip_file)
            zip.write(path, path_in_zip_file)
        print "Created zip file '%s'." % (zip_target_path)

    def _get_paths(self):
        paths = []
        for dir, pattern in [(self._libdir, '*.jar'),
                             (self._testdir, '*.*'), (self._demodir, '*.*')]:
            paths.extend(glob.glob(os.path.join(dir, pattern)))
        return paths

def package():
    """Package RemoteApplications with dependencies in a jar file"""
    PackageBuilder().package()

def test():
    """Run the acceptance tests"""
    TestRunner().execute()

def demo():
    """Package demo as zip file"""
    DemoPackager().execute()

def doc():
    """Create library documentation with libdoc"""
    TestRunner().add_dependencies_to_classpath()
    libdoc = _join(ROOTDIR, 'lib', 'libdoc.py')
    output = _join(TARGET, 'RemoteApplications.html')
    lib = _join(ROOTDIR, 'src', 'main', 'python', 'RemoteApplications.py')
    command = 'jython -Dpython.path=%s %s --output %s %s' % (TestRunner()._robot_installation_path(), libdoc, output, lib)
    _shell(command.split())

def dist():
    """Create package, demo and docs"""
    package()
    demo()
    doc()

def _shell(cmd):
    print 'Running %s' % ' '.join(cmd)
    return subprocess.call(cmd, shell=os.name=='nt')

def _format_tasks():
    return '\n'.join(t.__name__ + '\n\t' + t.__doc__ for t in _tasks())

def _tasks():
    def _is_task(item):
        return inspect.isfunction(item) and not item.__name__.startswith('_')
    return filter(_is_task, globals().values())


if __name__ == '__main__':
    try:
        {'package': package,
         'demo': demo,
         'test': test,
         'doc': doc,
         'dist': dist}[sys.argv[1]]()
    except (KeyError, IndexError):
        print "Usage:  build.py task\n\nAvailable tasks:\n", _format_tasks()

