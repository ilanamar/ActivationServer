
set JAVA_HOME=c:\dev\tools\jdk-1.4.2_05\jre

set CLASSPATH=
set CLASSPATH=%CLASSPATH%;./lib/bridge2nagra.jar
set CLASSPATH=%CLASSPATH%;./lib/classes12.zip
set CLASSPATH=%CLASSPATH%;./lib/aqapi.jar

%JAVA_HOME%\bin\java -classpath %CLASSPATH% -Dactivation-server.home=. com.gc.addrs.nagra.test.Bridge2NagraTest

pause
