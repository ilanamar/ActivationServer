
JAVA_HOME="/user1/Bridge2Nds/jre141"

CLASSPATH=
CLASSPATH=${CLASSPATH}:./test
CLASSPATH=${CLASSPATH}:./lib/common.jar
CLASSPATH=${CLASSPATH}:./lib/bridge2nagra.jar
CLASSPATH=${CLASSPATH}:./lib/commons-logging.jar
CLASSPATH=${CLASSPATH}:./lib/log4j.jar
CLASSPATH=${CLASSPATH}:./lib/xerces.jar
CLASSPATH=${CLASSPATH}:./lib/castor.jar
CLASSPATH=${CLASSPATH}:./config

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} -Dactivation-server.home=. com.gc.addrs.nagra.test.NagraFaker

pause
