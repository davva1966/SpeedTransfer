;!@Install@!UTF-8!
Title="SpeedTransfer"
ExtractDialogText="Extracting SpeedTransfer..."
GUIFlags="32"
ExtractTitle="Extracting"
RunProgram="launcher\jre\bin\javaw.exe -Xms40m -Xmx1024m -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8001 -Dcom.ss.speedtransfer.executionenvironment=3 -jar launcher\app.jar \"%%T\" \"%%S\""
;!@InstallEnd@!