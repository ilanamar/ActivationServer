
JAVA_HOME="/user1/Bridge2Nds/jre141"

CLASSPATH=
CLASSPATH=${CLASSPATH}:./lib/bridge2nagra.jar
CLASSPATH=${CLASSPATH}:./lib/classes12.zip
CLASSPATH=${CLASSPATH}:./lib/aqapi.jar

${JAVA_HOME}/bin/java -classpath ${CLASSPATH} -Dactivation-server.home=. com.gc.addrs.nagra.test.Bridge2NagraTest

pause
