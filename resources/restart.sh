
#!/bin/bash

#this is assuming there's only one docgen-service running at a time, probably not the best way to find the process...
PID=""
PID=`ps -u www -o pid,cmd | grep "[d]ocgen-service" | head -n 1 | awk '{print $1}'`
if [ -n "$PID" ] ; then
    echo "killing process $PID"
    kill $PID
    sleep 5
    PID=""
    PID=`ps -u www -o pid,cmd | grep "[d]ocgen-service" | head -n 1 | awk '{print $1}'`
    if [ -n "$PID" ] ; then
        kill -9 $PID
    fi
fi
rm /home/www/.magicdraw/17.0/*.log*
echo "starting"
/data/www/docgen-service/bin/mdserver.sh /data/www/docgen-service/bin/config.properties > /data/www/docgen-service/bin/LOG &


# crontab: . /etc/profile; blah