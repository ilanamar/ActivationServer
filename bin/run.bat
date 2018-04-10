set JAVA_HOME=c:\dev\tools\jdk-1.4.2_05\jre

set CLASSPATH=./lib/common.jar
set CLASSPATH=%CLASSPATH%;./lib/jetty.jar
set CLASSPATH=%CLASSPATH%;./lib/servlet.jar
set CLASSPATH=%CLASSPATH%;./lib/bridge2nds.jar
set CLASSPATH=%CLASSPATH%;./lib/bridge2nagra.jar
set CLASSPATH=%CLASSPATH%;./lib/server.jar
set CLASSPATH=%CLASSPATH%;./lib/xerces.jar
set CLASSPATH=%CLASSPATH%;./lib/classes12.zip
set CLASSPATH=%CLASSPATH%;./lib/aqapi.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j-impl.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j-jmx.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j-remote.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j-rimpl.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j-rjmx.jar
set CLASSPATH=%CLASSPATH%;./lib/mx4j-tools.jar
set CLASSPATH=%CLASSPATH%;./lib/commons-logging.jar
set CLASSPATH=%CLASSPATH%;./lib/log4j.jar
set CLASSPATH=%CLASSPATH%;./lib/castor.jar
set CLASSPATH=%CLASSPATH%;./lib/activation.jar
set CLASSPATH=%CLASSPATH%;./lib/mail.jar
set CLASSPATH=%CLASSPATH%;./config

%JAVA_HOME%\bin\java -classpath %CLASSPATH% -Dactivation-server.home=. com.gc.addrs.server.ActivationServer

pause