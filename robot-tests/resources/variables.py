import glob as _glob
import os as _os


_pattern = _os.path.join(_os.path.dirname(__file__), '..', '..',
                         'target', '*-jar-with-dependencies.jar')
_paths = _glob.glob(_pattern)

if not _os.getenv('HOME') and _os.getenv('USERPROFILE'):
    _os.environ['HOME'] = _os.getenv('USERPROFILE')

if _paths:
    _paths.sort()
    JAVA_AGENT_JAR = _paths[-1]
else:
    raise RuntimeError('Please run "mvn assembly:assembly first')
