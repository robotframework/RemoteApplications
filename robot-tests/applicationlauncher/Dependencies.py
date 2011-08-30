from os import path, makedirs, popen
from shutil import copy
from glob import glob

def sh(command):
    process = popen(command)
    output = process.read()
    process.close()
    return output

class Dependencies:
    def copy_dependencies_to(self, dir):
        if not path.exists(dir):
            makedirs(dir)

        self._copy_maven_dependencies(dir)
        self._copy_test_keywords(dir)

    def _copy_maven_dependencies(self, dir):
        print '*DEBUG* copying maven dependencies'
        dependencies_txt = path.join(path.dirname(__file__), '..',
                                     '..', 'dependencies.txt')
        dependencies = open(dependencies_txt).read().splitlines()

        for dependency in dependencies:
            self._copy(dependency, dir)

    def _copy(self, src, dst):
        print "*DEBUG* Copying file '%s' to '%s'" % (src, dst)
        if src is None or not path.exists(src):
            raise RuntimeError("Path does not exist '%s'" % (src))
        copy(src, dst)

    def _copy_test_keywords(self, dir):
        print '*DEBUG* copying test keywords'
        if not self._keywords_jar():
            sh('mvn -f keywords-pom.xml package')

        self._copy(self._keywords_jar(), dir)

    def _keywords_jar(self):
        return self._find_jar('jvmconnector-keywords-*.jar')

    def _find_jar(self, jar_pattern):
        pattern = path.join(path.dirname(__file__), '..','..',
                            'target', jar_pattern)

        jar = glob(pattern)
        if jar:
            return jar[0]
        else:
            return None
