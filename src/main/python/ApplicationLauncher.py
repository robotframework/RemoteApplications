import re
import time
import sys
import __builtin__

from os import path, remove
from tempfile import gettempdir, mktemp

from java.lang import Class
from java.lang import System
from java.net import ServerSocket

from robot.utils import timestr_to_secs
from robot.running import NAMESPACES
from robot.running.importer import Importer

from org.robotframework.org.springframework.remoting import RemoteAccessException
from org.robotframework.org.springframework.beans.factory import BeanCreationException
from org.robotframework.org.springframework.remoting.rmi import RmiServiceExporter
from org.robotframework.org.springframework.remoting.rmi import RmiProxyFactoryBean

from org.robotframework.jvmconnector.server import *
from org.robotframework.jvmconnector.client import RobotRemoteLibrary
from org.robotframework.jvmconnector.launch.jnlp import JnlpEnhancer
from org.robotframework.jvmconnector.common import DataBasePaths

from robot.libraries.OperatingSystem import OperatingSystem
from robot.libraries.BuiltIn import BuiltIn

class RemoteLibrary:

    def __init__(self, uri):
        self.uri = uri
        self._open_connection()

    def _open_connection(self):
        self.remote_lib = RobotRemoteLibrary(self.uri)
        
    def get_keyword_names(self):
        return list(self.remote_lib.getKeywordNames())

    def run_keyword(self, name, args):
        try:
            return self.remote_lib.runKeyword(name, args)
        except RemoteAccessException:
            print '*DEBUG* Reconnecting to remote library.' 
            self._open_connection()
            return self.remote_lib.runKeyword(name, args)


class RmiWrapper:

    def __init__(self, library_importer_publisher):
        self.library_importer_publisher = library_importer_publisher
        self.class_loader = Class

    def export_rmi_service_and_launch_application(self, application, args):
        self.library_importer_publisher.start(DATABASE)
        self.class_loader.forName(application).main(args)


class InvalidURLException(Exception):
    pass

DATABASE = DataBasePaths().getLaunchedFile()

class ApplicationLauncher:
    """A library for starting java application or Java Webstart application and importing
    remote libraries for operating it. The application is started as a separate
    process.

    
    """

    ROBOT_LIBRARY_SCOPE = 'TEST SUITE'

    def __init__(self, application, timeout='60 seconds', libdir=''):
        """ApplicationLauncher takes one mandatory and two optional arguments.

        `application` is a required argument, it is the name of the main
        class (the class that has the main method) or the url to the Java
        Webstart jnlp descriptor file. In case the `application` is a jnlp url
        `libdir` must be provided.

        `timeout` is the timeout used to wait for importing a remote library.

        `libdir` is the path to the directory which should contain jars which
        should contain all the dependencies required for running the tests. In
        another words these jar files should contain jvmconnector jar and
        libraries that you want to remotely import (packaged in jars).
        """
        self.application = application
        self.timeout = timestr_to_secs(timeout or '60')
        self.libdir = libdir
        self.builtin = BuiltIn()
        self.operating_system = OperatingSystem()
        self.rmi_url = None
        self._assert_invariants()

    def start_application(self, args='', jvm_args=''):
        """Starts the application with given arguments.

        `args` optional application arguments..
        `jvm_args` optional jvm arguments.

        Example:
        | Start Application | one two three | -Dproperty=value |
        """
        command = self._create_command(args, jvm_args)
        self.operating_system.start_process(command)
        self.application_started()
    
    def import_remote_library(self, library_name, *args):
        """Imports a library with given arguments for the application.
        
        Application needs to be started before using this keyword. In case the
        application is started externally, `Application Started` keyword has
        to be used beforehand. In case there is multiple applications, there
        is need to have one ApplicationLauncher per application. In that case,
        starting application and library imports needs to be in sequence. It is 
        not possible to start multiple applications and then import libraries
        to those.

        Examples:

        | Start Application | arg1 |  
        | Import Remote Library | SwingLibrary |
        
        or

        | Application Started | 
        | Import Remote Library | SwingLibrary |
        """
        library_url = self._run_remote_import(library_name)
        newargs = self._add_name_to_args_if_necessary(library_name, args)
        self._prepare_for_reimport_if_necessary(library_url, *newargs) 
        self.builtin.import_library('ApplicationLauncher.RemoteLibrary',
                                    library_url,
                                    *newargs)

    def close_application(self):
        """Closes the active application.
        
        If same application is opened multiple times, only the latest one is
        closed. Therefore you should close the application before starting it
        again."""
        rmi_client = self._connect_to_base_rmi_service()
        self.rmi_url = None
        try:
            rmi_client.getObject().closeService()
        except RemoteAccessException:
            return
        raise RuntimeError('Could not close application.')
            

    def application_started(self):
        """Notifies ApplicationLauncher that application is launched
        externally.
        
        Required before taking libraries into use with `Import Remote Library` 
        when application is started with ApplicationLauncher.py script.
        """
        self.rmi_url = None
        self._connect_to_base_rmi_service()

    def _create_command(self, args, jvm_args):
        if (self._is_jnlp_application()):
            jnlp = JnlpEnhancer(self.libdir).createRmiEnhancedJnlp(self.application)
            return 'javaws %s %s'  % (jvm_args, jnlp)
        else:
            pythonpath = self._get_python_path()
            out_file, err_file = self._get_output_files()
            return 'jython -Dpython.path="%s" %s "%s" %s %s 1>%s 2>%s' % (pythonpath,
                   jvm_args, __file__, self.application, args, out_file, err_file)

    def _is_jnlp_application(self):
        return self.application.startswith('http') or path.isfile(self.application)
        
    def _get_output_files(self):
        out_file = mktemp('%s.out' % self.application)
        err_file = mktemp('%s.err' % self.application)
        return out_file, err_file 

    def _get_python_path(self):
        for path_entry in sys.path:
            if path.exists(path.join(path_entry, 'robot')):
                return path_entry

    def _add_name_to_args_if_necessary(self, library_name, args):
        if len(args) >= 2 and args[-2].upper() == 'WITH NAME':
            return args

        return sum((args,), ('WITH NAME', library_name))

    def _prepare_for_reimport_if_necessary(self, library_url, *args):
        lib = Importer().import_library('ApplicationLauncher.RemoteLibrary',
                                        sum((args,), (library_url,)))
        testlibs = NAMESPACES.current._testlibs
        if testlibs.has_key(lib.name):
            testlibs.pop(lib.name)

    def _connect_to_base_rmi_service(self): 
        start_time = time.time()
        while time.time() - start_time < self.timeout:
            url = self._retrieve_base_rmi_url()
            try:
                return self._create_rmi_client(url)
            except (BeanCreationException, RemoteAccessException,
                    InvalidURLException):
                time.sleep(2)
        raise RuntimeError('Could not connect to application %s' % self.application)

    def _run_remote_import(self, library_name): 
        try:
            rmi_client = self._connect_to_base_rmi_service()
            return rmi_client.getObject().importLibrary(library_name)
        except (BeanCreationException, RemoteAccessException):
            raise RuntimeError('Could not connect to application %s' % self.application)

    def _retrieve_base_rmi_url(self):
        if self.rmi_url:
            return self.rmi_url

        return RmiInfoStorage(DATABASE).retrieve()

    def _create_rmi_client(self, url):
        if not re.match('rmi://[^:]+:\d{1,5}/.*', url):
            raise InvalidURLException()

        rmi_client = RmiProxyFactoryBean(serviceUrl=url,
                                         serviceInterface=LibraryImporter)
        rmi_client.prepare()
        rmi_client.afterPropertiesSet()
    
        self._save_base_url_and_clean_db(url)
        return rmi_client
    
    def _save_base_url_and_clean_db(self, url):
        self.rmi_url = url
        if path.exists(DATABASE):
            remove(DATABASE)
    
    def _assert_invariants(self):
        if self._is_jnlp_application():
            self._assert_libdir_is_correct()

    def _assert_libdir_is_correct(self):
        if len(self.libdir) == 0:
            raise RuntimeError('Library directory required for test dependencies.')
        else:
            if not path.isdir(self.libdir):
                raise RuntimeError("Library directory '%s' doesn't exist." % self.libdir)

if __name__ == '__main__':
    if len(sys.argv[1:]) >= 1:
        wrapper = RmiWrapper(RmiService())
        wrapper.export_rmi_service_and_launch_application(sys.argv[1],
                                                          sys.argv[2:])

