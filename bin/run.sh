JAVA_HOME="/user1/Bridge2Nds/jre141"

CLASSPATH=
CLASSPATH=${CLASSPATH}:./lib/jetty.jar
CLASSPATH=${CLASSPATH}:./lib/servlet.jar
CLASSPATH=${CLASSPATH}:./lib/common.jar
CLASSPATH=${CLASSPATH}:./lib/bridge2nds.jar
CLASSPATH=${CLASSPATH}:./lib/bridge2nagra.jar
CLASSPATH=${CLASSPATH}:./lib/server.jar
CLASSPATH=${CLASSPATH}:./lib/xerces.jar
CLASSPATH=${CLASSPATH}:./lib/classes12.zip
CLASSPATH=${CLASSPATH}:./lib/aqapi.jar
CLASSPATH=${CLASSPATH};./lib/mx4j.jar
CLASSPATH=${CLASSPATH};./lib/mx4j-impl.jar
CLASSPATH=${CLASSPATH};./lib/mx4j-jmx.jar
CLASSPATH=${CLASSPATH};./lib/mx4j-remote.jar
CLASSPATH=${CLASSPATH};./lib/mx4j-rimpl.jar
CLASSPATH=${CLASSPATH};./lib/mx4j-rjmx.jar
CLASSPATH=${CLASSPATH};./lib/mx4j-tools.jar
CLASSPATH=${CLASSPATH}:./lib/commons-logging.jar
CLASSPATH=${CLASSPATH}:./lib/log4j.jar
CLASSPATH=${CLASSPATH}:./lib/castor.jar
CLASSPATH=${CLASSPATH}:./lib/activation.jar
CLASSPATH=${CLASSPATH}:./lib/mail.jar
CLASSPATH=${CLASSPATH}:./config

${JAVA_HOME}/bin/java -classpath ${CLASSPATH}-Dbridge2nds.home=${BRIDGE_HOME} -Dactivation-server.home=. com.gc.addrs.server.ActivationServer
