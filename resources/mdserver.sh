#!/bin/bash
MD="/Applications/MagicDraw UML 17.0.2"

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

QVT="$MD/plugins/com.nomagic.magicdraw.qvt"
IMCE="$MD/plugins/gov.nasa.jpl.magicdraw.imce"
IMCEQVT="$MD/plugins/gov.nasa.jpl.magicdraw.qvto.library"
MDREPORT="$MD/plugins/com.nomagic.magicdraw.reportwizard"
MDDOCGEN="$MD/plugins/gov.nasa.jpl.mbee.docgen"
MDTABLE="$MD/plugins/com.nomagic.magicdraw.diagramtable"
MDDEPEN="$MD/plugins/com.nomagic.magicdraw.dependencymatrix"
MDLIB="$MD/lib"
MDAUTO="$MD/plugins/com.nomagic.magicdraw.automaton"
MDAUTOLIB="$MD/plugins/com.nomagic.magicdarw.automaton/lib"
MDPYTHONENGINE="$MD/plugins/com.nomagic.magicdraw.jpython/jython2.5.1"

MDCP="$MDLIB/*:$MDLIB/graphics/*:$MDLIB/webservice/*"
MDREPORTCP="$MDREPORT/*:$MDREPORT/lib/*:$MDREPORT/extensions/*"
MDTABLECP="$MDTABLE/*"
MDDEPENCP="$MDDEPEN/*"
MDJYTHONCP="$MDPYTHONENGINE/jython.jar"
MDAUTOCP="$MDAUTO/*:$MDAUTO/lib/*"
QVTCP="$QVT/*:$QVT/lib/*"
IMCECP="$IMCE/lib/*"
IMCEQVTCP="$IMCEQVT/lib/*"
DOCGENCP="$MDDOCGEN/*:$MDDOCGEN/lib/*"
LOCALCP="$SCRIPTDIR/lib/*"

CLASSPATH="$SCRIPTDIR/docgenserver.jar:$LOCALCP:$MDLIB/patch.jar:$MDCP:$MDREPORTCP:$MDDEPENCP:$MDTABLECP:$MDAUTOCP:$MDJYTHONCP:$QVTCP:$IMCECP:$IMCEQVTCP:$DOCGENCP"

MAINCLASS=gov.nasa.jpl.mbee.mdserver.CommandLineServer
exec java -Xmx1500M -XX:PermSize=60M -XX:MaxPermSize=200M -Djava.io.tmpdir="$MD/tmp" -Dlauncher.properties.file="$MD/bin/mduml.properties" -DLOCALCONFIG=false -Dinstall.root="$MD" -cp "$CLASSPATH" $MAINCLASS "$1"