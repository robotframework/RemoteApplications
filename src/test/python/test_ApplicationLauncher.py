import unittest
import os
import sys

from robot.utils.asserts import *
from robot.running import NAMESPACES
from java.net import ServerSocket

from org.robotframework.jvmconnector.mocks import SomeClass
from org.robotframework.jvmconnector.server import *

from ApplicationLauncher import *
import tempfile

application = 'org.robotframework.jvmconnector.mocks.SomeClass'

class TestApplicationLauncherStartingApplication(unittest.TestCase):

    def setUp(self):
        self.rmi_launcher = ApplicationLauncher(application)
        self.rmi_launcher._get_output_files = lambda: ('out', 'err')
        self.rmi_launcher.db_path = '/tempfile'
        self.rmi_launcher.application_started = self._fake_application_started
        self.os_library = _FakeOperatingSystemLibrary()
        self.rmi_launcher.operating_system = self.os_library

    def test_starts_application(self):
        self.rmi_launcher.start_application()
        assert_equals(self._get_expected_command(), self.os_library.command)
        assert_true(self.application_started)

    def tests_passes_arguments(self):
        self.rmi_launcher.start_application('one two three', '-Done=two -Dthree=four')
        expected_command = self._get_expected_command('one two three', '-Done=two -Dthree=four')
        assert_equals(expected_command, self.os_library.command)

    def _get_expected_command(self, args='', jvm_args=''):
        current_pythonpath = self._get_current_pythonpath()
        script_path = self._get_path_to_script()
        template = 'jython -Dpython.path="%s" %s "%s" %s %s 1>out 2>err'
        return template % (current_pythonpath, jvm_args, script_path,
                           application, args)

    def _get_current_pythonpath(self):
        for path_entry in sys.path:
            if os.path.exists(os.path.join(path_entry, 'robot')):
                return path_entry

    def _get_path_to_script(self):
        base = os.path.abspath(os.path.normpath(os.path.split(sys.argv[0])[0]))
        return '%s/src/main/python/ApplicationLauncher.py' % base

    def _fake_application_started(self):
        self.application_started = True

class TestApplicationLauncherImportingLibrary(unittest.TestCase):
    def setUp(self):
        class _FakeNamespace:
            _testlibs = {}
        NAMESPACES.current = _FakeNamespace()
        self.rmi_launcher = ApplicationLauncher(application)
        self.builtin_library = _FakeBuiltInLibrary()
        self.rmi_launcher.builtin = self.builtin_library
        self.rmi_launcher._run_remote_import = self._fake_remote_import
        self.rmi_launcher._prepare_for_reimport_if_necessary = lambda x,*args: None
        self.library = None

    def test_imports(self):
        self.rmi_launcher.import_remote_library('SomeLibrary', 'WITH NAME', 'someLib')

        assert_equals('SomeLibrary', self.library_name)
        assert_equals('ApplicationLauncher.RemoteLibrary', self.builtin_library.library)
        expected_args = ('rmi://someservice', 'WITH NAME', 'someLib')
        assert_equals(expected_args, self.builtin_library.arguments)

    def test_adds_name_when_with_name_is_not_used(self):
        self.rmi_launcher.import_remote_library('SomeLibrary')

        assert_equals('SomeLibrary', self.library_name)
        assert_equals('ApplicationLauncher.RemoteLibrary', self.builtin_library.library)
        expected_args = ('rmi://someservice', 'WITH NAME', 'SomeLibrary')
        assert_equals(expected_args, self.builtin_library.arguments)

    def test_parses_timestring(self):
        rmi_launcher = ApplicationLauncher(application, '1 second')
        assert_equals(1, rmi_launcher.timeout)

    def _fake_remote_import(self, library_name):
        self.library_name = library_name
        return 'rmi://someservice'


class TestRmiWrapper(unittest.TestCase):

    def setUp(self):
        self.library_importer_publisher = _FakePublisher()
        self.wrapper = RmiWrapper(self.library_importer_publisher)

    def test_exports_rmi_service_and_launches_application(self):
        class_loader = _FakeClassLoader()
        self.wrapper.class_loader = class_loader
        args = ['one', 'two']
        self.wrapper.export_rmi_service_and_launch_application(application, args)
        
        assert_true(DATABASE, self.library_importer_publisher.rmi_info_storage)
        assert_equals(application, class_loader.name)
        assert_equals(args, class_loader.main_args)

    def test_application_is_launched_by_invoking_java_classes_main_method(self):
        args = ['one', 'two']
        self.wrapper.export_rmi_service_and_launch_application(application, args)
        assert_equals(DATABASE, self.library_importer_publisher.rmi_info_storage)
        assert_equals(args, [i for i in SomeClass.args])


class _FakeBuiltInLibrary:
    def import_library(self, library, *arguments):
        self.library = library
        self.arguments = arguments
        
class _FakeOperatingSystemLibrary:
    def start_process(self, command):
        self.command = command

class _FakePublisher:
    def start(self, rmi_info_storage):
        self.rmi_info_storage = rmi_info_storage

class _FakeClassLoader:
    def __init__(self, class_=None):
        self.class_ = class_ or self

    def forName(self, name):
        self.name = name
        return self.class_

    def main(self, args):
        self.main_args = args


if __name__ == '__main__':
    unittest.main()
