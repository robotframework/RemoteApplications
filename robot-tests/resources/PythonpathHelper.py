import sys
import os


class PythonpathHelper:
    def get_python_path(self):
        for path_entry in sys.path:
            if os.path.exists(os.path.join(path_entry, 'robot')):
                return path_entry
