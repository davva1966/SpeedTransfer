;!@Install@!UTF-8!
Title="SpeedTransfer"
ExtractDialogText="Extracting SpeedTransfer..."
GUIFlags="32"
ExtractTitle="Extracting"
RunProgram="launcher\jre\bin\javaw.exe -Xms512m -Xmx2048m -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8001 -Dcom.ss.speedtransfer.executionenvironment=3 -jar launcher\app.jar \"%%T\" \"%%S\""
;!@InstallEnd@!