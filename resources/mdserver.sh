#!/bin/bash
MD="/Applications/MagicDraw UML 17.0"

#export DISPLAY=localhost:1.0

if [ $# -ne 1 ]; then
    echo "You must give the config.properties file"
    exit 1
fi
if [ ! -f "$1" ]; then 
	echo "$1 does not exist!"
    exit 1
fi

PRG="$0"
progname=`basename "$0"`

# need this for relative symlinks
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

SCRIPTDIR=`dirname "$PRG"`

# make it fully qualified
SCRIPTDIR=`cd "$SCRIPTDIR" && pwd`
echo $SCRIPTDIR


MDREPORT="$MD/plugins/com.nomagic.magicdraw.reportwizard"
MDDOCGEN="$MD/plugins/gov.nasa.jpl.mbee.docgen"
MDTABLE="$MD/plugins/com.nomagic.magicdraw.diagramtable"
MDDEPEN="$MD/plugins/com.nomagic.magicdraw.dependencymatrix"
MDLIB="$MD/lib"
MDGLIB="$MDLIB/graphics"
MDWLIB="$MDLIB/webservice"
MDAUTO="$MD/plugins/com.nomagic.magicdraw.automaton"
MDAUTOLIB="$MD/plugins/com.nomagic.magicdarw.automaton/lib"
MDPYTHONENGINE="$MD/plugins/com.nomagic.magicdraw.jpython/jython2.5.1"

MDCP=$(JARS=("$MDLIB"/*.jar); IFS=:; echo "${JARS[*]}")
MDGCP=$(JARS=("$MDGLIB"/*.jar); IFS=:; echo "${JARS[*]}")
MDWCP=$(JARS=("$MDWLIB"/*.jar); IFS=:; echo "${JARS[*]}")
MDREPORTCP="$MDREPORT/reportwizard_api.jar:$MDREPORT/reportwizard.jar"
MDREPORTLIBCP=$(JARS=("$MDREPORT/lib"/*.jar); IFS=:; echo "${JARS[*]}")
MDREPORTEXCP=$(JARS=("$MDREPORT/extensions"/*.jar); IFS=:; echo "${JARS[*]}")
MDTABLECP="$MDTABLE/diagramtable.jar:$MDTABLE/diagramtable_api.jar"
MDDEPENCP="$MDDEPEN/dependencymatrix_api.jar"
MDJYTHONCP="$MDPYTHONENGINE/jython.jar"
MDAUTOCP=$(JARS=("$MDAUTO"/*.jar); IFS=:; echo "${JARS[*]}")
MDAUTOLIBCP="$MDAUTOLIB/asm-3.3.jar"
DOCGENCP="$MDDOCGEN/DocGen-plugin.jar:$MDDOCGEN/lib/jsoup-1.6.1.jar:$MDDOCGEN/lib/jgrapht-0.8.3-jdk1.6.jar"
LOCALCP=$(JARS=("$SCRIPTDIR/lib"/*.jar); IFS=:; echo "${JARS[*]}")

CLASSPATH="$SCRIPTDIR/docgenserver.jar:$LOCALCP:$MDLIB/patch.jar:$MDCP:$MDGCP:$MDWCP:$MDREPORTCP:$MDREPORTLIBCP:$MDREPORTEXCP:$MDDEPENCP:$MDTABLECP:$MDAUTOLIBCP:$MDAUTOCP:$MDJYTHONCP:$DOCGENCP"

MAINCLASS=gov.nasa.jpl.mbee.mdserver.CommandLineServer

exec java -Xmx1500M -XX:PermSize=60M -XX:MaxPermSize=200M -Dlauncher.properties.file="$MD/bin/mduml.properties" -DLOCALCONFIG=true -Dinstall.root="$MD" -cp "$CLASSPATH" $MAINCLASS "$1"