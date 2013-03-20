from SimpleHTTPServer import SimpleHTTPRequestHandler
from os.path import join
from SocketServer import TCPServer, ThreadingMixIn
import threading

HTTP_PORT = 14563

class CustomHandler(SimpleHTTPRequestHandler):

    def log_message(self, format, *args):
        pass

    def translate_path(self, target):
        target = target[1:] if target.startswith('/') else target
        return join('javaws_app', target)


class FileServer(ThreadingMixIn, TCPServer):
    allow_reuse_address = True

    def __init__(self):
        pass

    def start(self):
        TCPServer.__init__(self, ('localhost', int(HTTP_PORT)), CustomHandler)
        server_thread = threading.Thread(target=self.serve_forever)
        server_thread.daemon = True
        server_thread.start()

    def stop(self):
        self.server_close()
        print "Server stopped"

