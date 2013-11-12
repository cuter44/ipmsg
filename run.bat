if "%1"=="--debug" (
  java -Xdebug -Xrunjdwp:transport=dt_socket,address=8288,suspend=y,server=y -cp ./bin;./lib/fastjson-1.1.35.jar com.github.cuter44.chat.ChatCli
) else (
  java -cp ./bin;./lib/fastjson-1.1.35.jar com.github.cuter44.chat.ChatCli
)
