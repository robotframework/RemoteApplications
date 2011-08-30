#!/usr/bin/env python

import glob
import unittest
import os
import sys
import re
from tempfile import gettempdir

base = os.path.abspath(os.path.normpath(os.path.split(sys.argv[0])[0]))
python_main = os.path.join(base, 'src', 'main', 'python')
python_test = os.path.join(base, 'src', 'test', 'python')
testfile = re.compile('^test_.*\.py$', re.IGNORECASE)          

def get_tests(directory):
    sys.path.append(directory)
    tests = []
    modules = []
    for name in os.listdir(directory):
        if name.startswith('.'): continue
        fullname = os.path.join(directory, name)
        if os.path.isdir(fullname):
            tests.extend(get_tests(fullname))
        elif testfile.match(name):
            modname = os.path.splitext(name)[0]
            modules.append(__import__(modname))
    tests.extend([ unittest.defaultTestLoader.loadTestsFromModule(module)
                   for module in modules ])
    return tests

def exists(file_name):
    file = os.path.join(base, file_name)
    return os.path.exists(file)

def sh(command):
    process = os.popen(command)
    output = process.read()
    process.close()
    return output

def add_src_and_test_to_pythonpath():
    for path in [ python_main, python_test ]:
        if path not in sys.path:
            sys.path.insert(0, path)
    os.environ['PYTHONPATH'] = os.pathsep.join(sys.path)

def add_dependencies_to_classpath():
    if not exists('dependencies.txt'):
        os.environ['MAVEN_OPTS'] = '-DoutputAbsoluteArtifactFilename=true'
        mvn_output = sh('mvn dependency:list').splitlines()

        jars = [re.sub('.*:((:?C:)?)', '\\1', file) for file in mvn_output if re.search('jar', file)]
        dependencies_txt = open(os.path.join(base, 'dependencies.txt'), 'w')
        for jar in jars:
            if exists(jar):
                dependencies_txt.write(jar + '\n')
        dependencies_txt.flush()

    test_classes = os.path.join('target', 'test-classes')
    if not exists(test_classes):
        sh('mvn test-compile')

    dependencies =  [get_jvmconnector_jar()] + [test_classes] + get_test_deps()
    os.environ['CLASSPATH'] = os.pathsep.join(dependencies)

def get_test_deps():
    deps = open('dependencies.txt', 'rb').read().splitlines()
    return [ dep for dep in deps if 'swinglibrary' in dep or 'org/mortbay' in dep ]

def get_jvmconnector_jar():
    pattern = os.path.join(os.path.dirname(__file__),
                           'target', '*-jar-with-dependencies.jar')
    paths = glob.glob(pattern)
    if paths:
        paths.sort()
        return paths[-1]
    else:
        raise RuntimeError('Please run "mvn assembly:assembly first')

def get_robot_installation_path():
    for path in sys.path:
        if os.path.exists(os.path.join(path, 'robot')):
            return path

def run_unit_tests():
    sys.path.append(python_main)
    tests = get_tests(os.path.join(base, 'src','test', 'python'))
    suite = unittest.TestSuite(tests)
    runner = unittest.TextTestRunner(descriptions=0, verbosity=1)
    result = runner.run(suite)
    rc = len(result.failures) + len(result.errors)
    if rc > 250: rc = 250
    return rc

def run_robot_tests(args):
    runner = os.path.join(get_robot_installation_path(), 'robot', 'runner.py')
    args_as_string = ' '.join(args)
    command = 'jython -Dpython.path="%s" "%s" --debugfile debug.txt --loglevel TRACE --noncritical javaagent --outputdir %s %s' % (get_robot_installation_path(), runner, gettempdir(), args_as_string)
    return os.system(command)

def run_unit_tests_with_jython():
    return os.system('jython -Dpython.path=%s %s' % (get_robot_installation_path(), __file__))


if __name__ == '__main__':
    if os.name == 'java':
        rc = run_unit_tests()
    else:
        add_src_and_test_to_pythonpath()
        add_dependencies_to_classpath()
        if len(sys.argv[1:]) > 0:
            rc = run_robot_tests(sys.argv[1:])
        else:
            rc = run_unit_tests_with_jython()
    sys.exit(rc >> 8)
