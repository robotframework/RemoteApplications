import os
import re
import time

from java.util.jar import JarFile
from java.util.zip import ZipException
from java.io import IOException, FileNotFoundException

from robot.utils import eq, normalize, NormalizedDict, seq2str, timestr_to_secs
from robot.running import NAMESPACES
from robot.running.namespace import IMPORTER
try:
    from robot.running.testlibraries import TestLibrary
except ImportError:
    #Available only from RF 2.5 onwards. 
    pass
from robot.libraries.BuiltIn import BuiltIn
from robot.libraries.OperatingSystem import OperatingSystem

from org.robotframework.jvmconnector.org.springframework.beans.factory import BeanCreationException
from org.robotframework.jvmconnector.org.springframework.remoting import RemoteAccessException
from org.robotframework.jvmconnector.org.springframework.remoting.rmi import RmiProxyFactoryBean

from org.robotframework.jvmconnector.client import RobotRemoteLibrary
from org.robotframework.jvmconnector.server import RmiInfoStorage, LibraryImporter
from org.robotframework.jvmconnector.common import DataBasePaths

class InvalidURLException(Exception):
    pass


class RemoteLibrary:

    def __init__(self, name, uri):
        self.name = name
        self._uri = uri
        self._open_connection()
        self._keywords = None

    def _open_connection(self):
        self._remote_lib = RobotRemoteLibrary(self._uri)

    def get_keyword_names(self):
        if self._keywords is None:
            self._keywords = list(self._remote_lib.getKeywordNames())
        return self._keywords

    def has_keyword(self, name):
        return name in self._keywords

    def run_keyword(self, name, args):
        try:
            self._remote_lib.ping()
        except RemoteAccessException:
            print "*DEBUG* Reconnecting"
            self._open_connection()
        return self._remote_lib.runKeyword(name, args)


class Applications:
    _database = DataBasePaths(True).getConnectedFile()

    def __init__(self):
        self._apps = NormalizedDict()
        self._old_apps = NormalizedDict()
        for alias, url in self._get_aliases_and_urls_from_db():
            self._old_apps[alias] = url

    def _get_aliases_and_urls_from_db(self):
        items = []
        for connection in self._read_lines(): 
            items.append(connection.rsplit('\t', 1))
        return items

    def _read_lines(self):
        if os.path.exists(self._database):
            f = open(self._database, 'rb')
            data = f.read().splitlines()
            f.close()
            return [line for line in data if self._is_valid_connection(line)]
        return []

    def _is_valid_connection(self, line):
        return len(line.rsplit('\t', 1)) == 2

    def add(self, alias, app):
        self._apps[alias] = app
        self._old_apps[alias] = app.rmi_url
        self._store()

    def _store(self):
        data = ['%s\t%s' % (alias, url) for alias, url in self._old_apps.items()]
        data_txt = '\n'.join(data)
        self._write(data_txt)
        print "*TRACE* Stored to connected applications database: \n%s" % data_txt

    def _write(self, data):
        f = open(self._database, 'wb')
        f.write(data)
        f.close()

    def has_connected_to_application(self, alias):
        return self._apps.has_key(alias)

    def get_application(self, alias):
        return self._apps[alias]

    def get_applications(self):
        return self._apps.values()

    def get_aliases(self):
        return self._apps.keys()

    def delete(self, alias):
        del(self._apps[normalize(alias)])
        del(self._old_apps[normalize(alias)])
        self._store()

    def delete_all_connected(self):
        for alias in self._apps.keys():
            self.delete(alias)

    def get_alias_for(self, application):
        for alias, app in self._apps.items():
            if app == application:
                return alias
        return None

    def get_url(self, alias):
        for name, url in self._get_aliases_and_urls_from_db():
            if eq(name, alias):
                return url
        return None


class RemoteApplication:
    ROBOT_LIBRARY_SCOPE = 'GLOBAL'
    _database = DataBasePaths().getLaunchedFile()

    def __init__(self):
        self._libs = []
        self._keywords = {}
        self.rmi_url = None
        self._rmi_client = None
        self.alias = None

    def application_started(self, alias, timeout='60 seconds', rmi_url=None):
        if self._rmi_client:
            raise RuntimeError("Application already connected")
        self.alias = alias
        timeout = timestr_to_secs(timeout or '60 seconds')
        self._rmi_client = self._connect_to_base_rmi_service(alias, timeout, rmi_url)
        print "*INFO* Connected to remote service at '%s'" % self.rmi_url

    def _connect_to_base_rmi_service(self, alias, timeout, rmi_url): 
        start_time = time.time()
        while time.time() - start_time < timeout:
            url = self._retrieve_base_rmi_url(rmi_url)
            try:
                print "*TRACE* Trying to connect to rmi url '%s'" % url
                return self._create_rmi_client(url)
            except (BeanCreationException, RemoteAccessException,
                    InvalidURLException):
                time.sleep(timeout/100.0)
        self._could_not_connect(alias)

    def _retrieve_base_rmi_url(self, url):
        return url or RmiInfoStorage(self._database).retrieve()

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
        if os.path.exists(self._database):
            os.remove(self._database)

    def _could_not_connect(self, alias):
        alias = alias or ''
        raise RuntimeError("Could not connect to application %s" % alias)

    def take_libraries_into_use(self, *library_names):
        for name in library_names:
            self.take_library_into_use(name)

    def take_library_into_use(self, library_name):
        #TODO: Add support for arguments
        self._check_connection()
        if self._library_already_in_use(library_name):
            print "*INFO* Library '%s' is already in use" % (library_name)
            return []
        library_url = self._import_remote_library(library_name)
        library = RemoteLibrary(library_name, library_url)
        self._libs.append(library)

    def _library_already_in_use(self, library_name):
        return library_name in [lib.name for lib in self._libs ]

    def _check_connection(self):
        if self._rmi_client is None:
            raise RuntimeError("No connection established. Use keyword " + 
                               "'Start Application' or 'Application Started' " +
                               "before this keyword.")

    def _import_remote_library(self, library_name): 
        return self._rmi_client.getObject().importLibrary(library_name)

    def close_application(self):
        self._check_connection()
        try:
            self._rmi_client.getObject().closeService()
        except RemoteAccessException:
            self._rmi_client = None
            return
        raise RuntimeError('Could not close application.')

    def is_connection_alive(self):
        try:
            return self._rmi_client.getObject().ping()
        except RemoteAccessException:
            return False

    def get_keyword_names(self):
        self._keywords = self._get_keywords_with_related_libraries()
        self._add_long_names_for_duplicates()
        keyword_names = self._keywords.keys()
        keyword_names.sort()
        return keyword_names

    def _get_keywords_with_related_libraries(self):
        keywords = {}
        for lib in self._libs:
            for keyword_name in lib.get_keyword_names():
                if keywords.has_key(keyword_name):
                    keywords[keyword_name].append(lib)
                else:
                    keywords[keyword_name] = [lib]
        return keywords

    def _add_long_names_for_duplicates(self):
        for kw, libs in self._keywords.items():
            if len(libs) > 1:
                for lib in libs:
                    self._keywords['%s.%s' % (lib.name, kw)] = [lib]

    def run_keyword(self, name, args):
        if len(self._keywords[name]) == 1:
            return self._keywords[name][0].run_keyword(name, args)
        self._raise_error_from_duplicate_keywords(name, self._keywords[name])
        

    def _raise_error_from_duplicate_keywords(self, name, libs):
        kw_names = ['%s.%s' % (lib.name, name) for lib in libs]
        raise RuntimeError("Keyword '%s' available from multiple remote libraries. Use: %s" % (name, seq2str(kw_names, lastsep=' or ')))


class RemoteApplicationsConnector:
    """

    RemoteApplications library is used for launching Java applications in a
    separate process and taking Robot Framework (RF) libraries into use to
    operate on them. This is useful when application does something that cannot
    be tested with RF when running inside the same JVM. Such cases are when
    System.exit is called in the SUT, when multiple applications running in
    separate JVMs need to be tested in parallel or when application is started
    using Java Web Start. RemoteApplications library is also suitable when
    application use embedded JVMs (meaning they use the JNI Invocation API to
    start the JVM), or when the startup is deeply nested in scripts. 

    Using RemoteApplications requires that jvm_connector jar file is in
    _CLASSPATH_ environment variable before starting RF test execution. 
    RemoteApplications works with Java 1.5 and newer. Following paragraphs 
    contain generic information about RemoteApplications library. See also 
    keywords' documentation for more detailed information.

	A Java application that can be started via command line, can be started 
	using the RemoteApplications library's `Start Application` keyword. Such 
	cases include starting a Java and Java Web Start processes, for example:
    - java -jar myapp.jar
    - jawaws http://robotframework.org/myapp.jnlp
    - myapp.exe
    
    Otherwise the robot agent (see the chapter below) and `Application Started` 
    keyword must be used to enable the testing capabilities. Such cases include:
    - starting Java Web Start application from a web page
    - running application remotely

    After the application is started, the needed test libraries must be taken 
    into use. That is done using `Take Library Into Use` and `Take Libraries 
    Into Use` keywords. After that, keywords are ready to be used. Note that the 
    libraries needs to be taken into use separately for each application.

    If multiple applications are started with RemoteApplications library,
    `Switch To Application` keyword can be used to define which application is
    currently active. Keywords always handle the currently active application.

    The application is closed using the keyword `Close Application`. Even if the 
    application is closed using some other keyword, RemoteApplications library 
    still needs to be informed about it, using the `Close Application` keyword. 
    The `Close All Applications` keyword can be used to close all the 
    applications.

    *NOTE:* RemoteApplications cannot be taken into use with _WITH NAME_
    functionality. However, there should not be a need for that as the
    RemoteApplications library can handle multiple applications.


    *ROBOT AGENT*

    Sometimes the application cannot be started locally from command line. In
    that case the Robot Agent needs to be used. It will start testing
    capabilities to the started application's JVM. Robot Agent works with Java
    Web Start applications and standalone java applications. It is taken into
    use by setting the _JAVA_TOOL_OPTIONS_ environment variable with value:

    _-javaagent:"${jvmconnector.jar}"="${testing_dependencies_dir}"[:PORT=${port}]_

    where _${jvmconnector.jar}_ is the path to the jvmconnector.jar and
    _${testing_dependencies_dir}_ is the path to the directory containing the
    test library jars. The optional _:PORT=${port}_ setting can be provided, 
    where the _:PORT=_ is separator, and _${port}_ defines the port number where 
    the service providing the testing capabilities is started.

    Examples of the setting command:

    _-javaagent:"jvmconnector-1.0.jar"="c:\\some\\testing\\lib"_

    _-javaagent:"~/some/testing/lib/jvmconnector-1.0.jar"="~/some/testing/lib":PORT=12345_

    When Robot Agent is used (RemoteApplications uses it internally) and the
    port parameter is not given, rmi_url from where the testing capabilities
    can be accessed is written to file
    `%HOME/.robotframework/jvmconnector/launched.txt` or to file
    `%APPDATA%\\RobotFramework\\jvmconnector\\launched.txt` on Windows. In case
    application is started on remote machine, this rmi_url needs to be given to
    `Application Started` keyword. When `Application Started` keyword
    establishes connection to the application, `launched.txt` is cleared and the
    active connection is added to `connected.txt` file (located in same
    directory as `launched.txt`). This file is used to connect active
    application in case the `connect_to_previously_launched_applications`
    argument is given when `importing` RemoteApplications library.

    *NOTE:* With Java 1.5 the testing dependencies cannot be added to the
    application's JVM with the Robot Agent. Therefore the test libraries need
    to be added to the classpath with some other means. Often it is possible to
    just add the needed jars to CLASSPATH environment variable. However, in case
    application's startup script clears the CLASSPATH or if application is 
    started using Java Web Start, setting CLASSPATH environment variable is not
    enough. In such situations the test libraries need to be included into the
    jvmconnector.jar file's classpath. This can be achieved by updating the
    jvmconnector.jar file's MANIFEST.MF file with command:

    `jar ufm jvmconnector.jar manifest.txt`

    For example, insert following line to the manifest.txt:

    Class-Path: ../relative/path/to/test_library.jar path/to/another_test_library.jar

    This example entry in the manifest.txt includes two test library jars to the 
    jvmconnector.jar file's MANIFEST.MF. Paths to the test libraries have to be 
    relative to the jvmconnector.jar and line needs to end with new line 
    character. See more from 
    http://java.sun.com/docs/books/tutorial/deployment/jar/downman.html
    """
    _database = DataBasePaths().getLaunchedFile()

    def __init__(self):
        self._initialize()
        ignore_methods = ['run_keyword', 'get_keyword_documentation',
                           'get_keyword_arguments', 'get_keyword_names',
                           'connect']
        self._kws = [ attr for attr in dir(self) if not attr.startswith('_') \
                     and attr not in ignore_methods ]
        self._use_previously_launched = False

    def _initialize(self):
        self._apps = Applications()
        self._active_app = None

    def connect(self, connect_to_previously_started_applications):
        self._use_previously_launched = connect_to_previously_started_applications

    def start_application(self, alias, command, timeout='60 seconds', 
                          lib_dir=None, port=None):
        """Starts the application, connects to it and makes it active application.

        `command` is the command used to start the application from the command
        line. It can be any command that finally starts JVM e.g. 'java -jar
        my_application.jar', javaws http://my.domain.fi/my_application.jnlp or 
        'start_my_app.bat'.

        `lib_dir` is path to the directory containing all the test library jar
        files which are required for running the tests. `lib_dir` is needed in
        case libraries are not in the CLASSPATH. When application is started
        using Java Web Start and Java version is 1.6 or higher, `lib_dir` is 
        mandatory. In case you are using 1.5 Java, you should package all these
        libraries to the `jvm_connector_jar` which is set to CLASSPATH before
        starting the test execution.

        When Java Web Start is used to start the application, there is need to
        allow permissions for the testing capabilities. Easiest way to do that
        is to add file .java.policy with following content to $HOME directory or
        %USERPROFILE% directory on Windows:
        | _grant {_
        |     _permission java.security.AllPermission;_
        | _};_
        
        `port` defines the port in which the testing capabilities are started
        on the application. By default port is selected randomly from available
        ports.

        Examples:
        | Start Application | App1 | java -jar my_application.jar | 30 seconds | \${CURDIR}${/}libs |
        | Start Application | App2 | my_application.exe |  | \${CURDIR}${/}libs | 12345 |

        *NOTE:* If the application is used to start other applications
        and those applications should be controlled with RemoteApplications, 
        port should NOT be given.

        To access application started in previous test run, you can set
        `connect_to_previously_launched_applications` when `Importing` library.
        If the application is available, this keyword connects to it and does
        NOT run the command for starting it.
        """
        self._alias_in_use(alias)
        self._clear_launched_apps_db(port)
        if not (self._use_previously_launched and self._apps.get_url(alias)):
            self._run_command_with_java_tool_options(command, lib_dir, port)
        rmi_url = self._get_rmi_url(alias, port)
        self.application_started(alias, timeout, rmi_url)

    def _alias_in_use(self, alias):
        if self._apps.has_connected_to_application(alias):
            raise RuntimeError("Application with alias '%s' already in use" % alias)

    def _clear_launched_apps_db(self, port):
        if not port and os.path.exists(self._database):
            os.remove(self._database)

    def _run_command_with_java_tool_options(self, command, lib_dir, port):
        orig_java_tool_options = self._get_java_tool_options()
        os.environ['JAVA_TOOL_OPTIONS'] =  self._get_java_agent(lib_dir, port)
        OperatingSystem().start_process(command)
        os.environ['JAVA_TOOL_OPTIONS'] = orig_java_tool_options

    def _get_java_tool_options(self):
        if os.environ.has_key('JAVA_TOOL_OPTIONS'):
            return os.environ['JAVA_TOOL_OPTIONS']
        return ''

    def _get_java_agent(self, lib_dir, port):
        lib_dir = lib_dir or ''
        jvm_connector_jar = self._get_jvm_connector_jar()
        port = port and ['PORT=%s' % port] or []
        return '-javaagent:"%s"="%s"' % (jvm_connector_jar, 
                                         os.path.pathsep.join(port + [lib_dir]))

    def _get_jvm_connector_jar(self):
        for jar_file in self._get_jars_from_classpath():
            if not os.path.exists(jar_file):
                continue
            try:
                manifest = JarFile(jar_file).getManifest()
                if not manifest:
                    continue
                main_attributes = manifest.getMainAttributes()
                premain_class = main_attributes.getValue('Premain-Class')
            except (ZipException, IOException, FileNotFoundException):
                continue
            if premain_class == 'org.robotframework.jvmconnector.agent.RmiServiceAgent':
                print "*TRACE* Found jvm_connector jar '%s'" % jar_file
                return os.path.abspath(jar_file)
        raise RuntimeError("Could not find jvmconnector jarfile from CLASSPATH")

    def _get_jars_from_classpath(self):
        jars = []
        if os.environ.has_key('CLASSPATH'):
            jars = jars + os.environ['CLASSPATH'].split(os.path.pathsep)
        if os.environ.has_key('classpath'):
            jars = jars + os.environ['classpath'].split(os.path.pathsep)
        return jars

    def _get_rmi_url(self, alias, port):
        url = port and 'rmi://localhost:%s/robotrmiservice' % port or None
        if self._use_previously_launched:
            url = url or self._apps.get_url(alias)
            "*DEBUG* Found url '%s' from previously started applications" % url
        return url

    def application_started(self, alias, timeout='60 seconds', rmi_url=None):
        """Connects to started application and switches to it.

        `alias` is the alias name for the application. When using multiple 
        applications alias is used to switch between them with keyword `Switch 
        To Application`.

        `timeout` is the time to wait the application to be started to the point
        where the testing capabilities are initialized and the connection to
        application can be established.

        `rmi_url` is url that can be used to connect to the application. When
        used locally there is usually no need to give the `rmi_url`. However,
        when the application is running on remote machine, the file based
        mechanism used to find the application is not enough and you need to
        provide the `rmi_url`. Format of the `rmi_url` is 
        'rmi://host:port/robotrmiservice'. See from `Introduction` info about
        Robot Agent.

        To access application started in previous test run, you can set
        `connect_to_previously_launched_applications` when `Importing` library.
        If the previously started application is available, this keyword 
        connects to it in case the 'rmi_url' is not given.

        Examples:
        | Application Started | App1 |  |  |
        | Application Started | App2 | 2 minutes | rmi://localhost:7000/robotrmiservice |
        """
        self._alias_in_use(alias)
        app = RemoteApplication()
        if self._use_previously_launched:
            rmi_url = rmi_url or self._apps.get_url(alias)
        app.application_started(alias, timeout, rmi_url)
        self._apps.add(alias, app)
        self._active_app = app

    def switch_to_application(self, alias):
        """Changes the application where the keywords are executed.

        `alias` is the name of the application and it have been given with the
        `Application Started` keyword.

        Example:
        | Switch To Application | App2 |
        | Do Something          | # Keyword operates App2 |
        | Switch To Application | App1 |
        | Do Something          | # Keyword operates App1 |
        """
        self._check_application_is_in_use(alias)
        self._active_app = self._apps.get_application(alias)

    def _check_application_is_in_use(self, alias):
        if not self._apps.has_connected_to_application(alias):
            raise RuntimeError("No Application with alias '%s' in use" % alias)

    def take_libraries_into_use(self, *library_names):
        """Takes the libraries into use at the remote application.

        `library_names` contains all the libraries that you want to take into
        use on the remote side. *NOTE:* See 'Start Application' for information
        how to provide library jar files.

        Example:
        | Start Application | App1 | java -jar my_application.jar | 2 min | \${CURDIR}${/}libs |
        | Take Libraries Into Use | MyLibrary | SwingLibrary |
        """
        self._check_active_app()
        self._active_app.take_libraries_into_use(*library_names)
        self._update_keywords_to_robot()

    def _check_active_app(self):
        if self._active_app is None:
            raise RuntimeError("No application selected")

    def _update_keywords_to_robot(self):
        # TODO: When Robot has better support for reimporting libraries,
        # update following code to use that approach. See RF issue 293.
        lib_name = 'RemoteApplications'
        self._remove_lib_from_current_namespace(lib_name)
        args = self._use_previously_launched and [self._use_previously_launched] or []
        self._remove_lib_from_importer(lib_name, args)
        BuiltIn().import_library(lib_name, *args)

    def _remove_lib_from_current_namespace(self, name):
        testlibs = NAMESPACES.current._testlibs
        if testlibs.has_key(name):
            del(testlibs[name])

    def _remove_lib_from_importer(self, name, args):
        if IMPORTER._libraries.has_key((name, tuple(args))):
            del(IMPORTER._libraries[(name, tuple(args))])
        elif IMPORTER._libraries.has_key((name, args)):
                self._delete_item_from_cache((name, args))
        else:
            #RF 2.5 support
            lib = TestLibrary(name, args, None, create_handlers=False)
            key = (name, lib.positional_args, lib.named_args)
            if IMPORTER._libraries.has_key(key):
                self._delete_item_from_cache(key)

    def _delete_item_from_cache(self, key):
        index = IMPORTER._libraries._keys.index(key)
        IMPORTER._libraries._keys.pop(index)
        IMPORTER._libraries._libs.pop(index)

    def take_library_into_use(self, library_name):
        """Takes given library into use.
        
        See `Take Libraries Into Use` keyword for more details.
        """
        #TODO: Add support for arguments
        self.take_libraries_into_use(library_name)

    def close_all_applications(self):
        """Closes all the applications."""
        for alias in self._apps.get_aliases():
            try:
                app = self._apps.get_application(alias)
                if app:
                    app.close_application()
            except RuntimeError:
                print "*WARN* Could not close application '%s'" % (alias)
        self._apps.delete_all_connected()
        self._initialize()

    def close_application(self, alias=None):
        """Closes application.

        If `alias` is given, closes application related to the alias.
        Otherwise closes the active application."""
        alias = alias or self._get_active_app_alias()
        print "*TRACE* Closing application '%s'" % alias
        self._check_application_is_in_use(alias)
        if self._apps.get_application(alias) == self._active_app:
            self._active_app = None
        try:
            self._apps.get_application(alias).close_application()
            print "*INFO* Closed application '%s'" % (alias)
        finally:
            self._apps.delete(alias)

    def _get_active_app_alias(self):
        self._check_active_app()
        return self._apps.get_alias_for(self._active_app)

    def application_should_be_connected(self):
        """Checks is there connection to the active application."""
        self._check_active_app()
        if not self._active_app.is_connection_alive():
            raise AssertionError("Could not connect to the application '%s'."
                                 % self._active_app.alias)

    def application_should_not_be_connected(self, timeout="2 seconds"):
        """Checks that there is no connection to the active application.
        
        `timeout` is time that the application is waited to be closed. Sometimes
        closing application takes some time and therefore it might take while
        before it is really closed."""
        self._check_active_app()
        timeout = timestr_to_secs(timeout)
        start_time = time.time()
        while time.time() - start_time < timeout:
            if not self._active_app.is_connection_alive():
                return
            time.sleep(timeout/100.0)
        raise AssertionError("There is connection to the application '%s'."
                             % self._active_app.alias)

    def get_keyword_names(self):
        kws = []
        for app in self._apps.get_applications():
            kws.extend(app.get_keyword_names())
        return kws + self._kws

    def run_keyword(self, name, args):
        method = self._get_method(name)
        if method:
            return method(*args)
        self._check_active_app()
        return self._active_app.run_keyword(name, args)

    def _get_method(self, name):
        if name in self._kws:
            return getattr(self, name)
        return None

    def get_keyword_arguments(self, name):
        method = self._get_method(name)
        if method:
            args, defaults, varargs = self._get_arg_spec(method)
            arguments = args[:len(args)-len(defaults)]
            defaults = [ '%s=%s' % (arg, default) for arg, default in zip(args[len(arguments):], defaults) ]
            varargs = varargs and ['*%s' % varargs] or []
            return arguments + defaults + varargs
        return ['*args']

    def _get_arg_spec(self, method):
        """Returns info about args in a tuple (args, defaults, varargs)

        args     - list of all accepted arguments
        defaults - list of default values
        varargs  - name of the argument accepting varargs or None
        """
        co = method.im_func.func_code
        number_of_args = co.co_argcount
        args = co.co_varnames[1:number_of_args] #drops 'self' from methods' args
        defaults = method.im_func.func_defaults or ()
        varargs = None
        if co.co_flags & 4:                      # 4 == CO_VARARGS
            varargs =  co.co_varnames[number_of_args]
        return list(args), list(defaults), varargs

    def get_keyword_documentation(self, name):
        method = self._get_method(name)
        if method:
            return method.__doc__
        return ''


class RemoteApplications:
    ROBOT_LIBRARY_SCOPE = 'GLOBAL'
    _connector = RemoteApplicationsConnector()
    __doc__ = _connector.__doc__

    def __init__(self, connect_to_previously_launched_applications=''):
        """
        `connect_to_previously_launched_applications` defines whether to connect
        applications that were started in previous test execution. By default 
        this feature is not in use. It can be taken into use by giving any value
        to library initialization like shown in below examples:

        | Library | RemoteApplications | # Does not connect to previously started applications |
        | Library | RemoteApplications | Reconnect | # Connects to previously started applications |

        Setting `connect_to_previously_launched_applications` fastens the test
        case development as you can open the application once, and after that,
        re-execute the test against the opened application without restarting
        it. Obviously, you need to disable closing the application to achieve
        this. Reconnecting can also be achieved by using fixed `rmi_url`, either
        by giving `port` argument to `Start Application` keyword or `rmi_url` to
        `Application Started` keyword.
        """
        self._connector.connect(connect_to_previously_launched_applications)

    def get_keyword_names(self):
        return self._connector.get_keyword_names()

    def run_keyword(self, name, args):
        return self._connector.run_keyword(name, args)

    def get_keyword_arguments(self, name):
        return self._connector.get_keyword_arguments(name)

    def get_keyword_documentation(self, name):
        return self._connector.get_keyword_documentation(name)
