dir /b /s .\src\*.java > source.lst
javac -cp ./lib/fastjson-1.1.35.jar -d ./bin @source.lst
del source.lst
