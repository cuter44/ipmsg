start run.bat --debug
jdb -connect com.sun.jdi.SocketAttach:port=8288,hostname=127.0.0.1 -sourcepath ./src
