
set JAVA_HOME=c:\dev\tools\jdk-1.4.2_05\jre

set CLASSPATH=
set CLASSPATH=%CLASSPATH%;./test
set CLASSPATH=%CLASSPATH%;./lib/common.jar
set CLASSPATH=%CLASSPATH%;./lib/bridge2nds.jar
set CLASSPATH=%CLASSPATH%;./lib/commons-logging.jar
set CLASSPATH=%CLASSPATH%;./lib/log4j.jar
set CLASSPATH=%CLASSPATH%;./lib/xerces.jar
set CLASSPATH=%CLASSPATH%;./lib/castor.jar
set CLASSPATH=%CLASSPATH%;./config

%JAVA_HOME%\bin\java -classpath %CLASSPATH% -Dactivation-server.home=. com.gc.addrs.nds.test.NdsFaker

pause
