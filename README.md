# MDServer
This is a wrapper around the Magicdraw client to trick it into behaving like a server, the main functionality is to connect to teamwork, open projects, and generate DocGen documents and send them to DocWeb upon receiving a request.

# Dependencies
The Magicdraw installation requires the MDK plugin installed (which includes DocGen), and thus the IMCE QVT libraries for QVT execution. Depending on the dependencies of the teamwork projects where the documents are, the Magicdraw installation also needs all custom profiles, userscripts, diagrams or plugins required to open the project.

In order to run headlessly, Xvfb should also be installed in order to provide a null display (http://www.x.org/archive/current/doc/man/man1/Xvfb.1.xhtml)

Tested with Magicdraw 17.0.2

## included libraries
* Jersey rest simple server (https://jersey.java.net/)
* json simple 1.1 (http://code.google.com/p/json-simple/)

# Setup
1. open the resources/mdserver.sh file
	1. Modify the MD variable so it points to the directory of MD install
	1. if desired, modify the memory allocated at the bottom
	1. create a "tmp" directory inside the magicdraw install (this is for the tmp files as magicdraw open projects, can be anywhere really), if tmp folder is elsewhere, modify the -Djava.io.tmpdir parameter at the bottom
	1. if running on headless server and Xvfb, uncomment the DISPLAY var to the null display
1. open the resources/config.properties file 
	1. tempDir is a tmp directory where DocGen outputs the generated document files
	1. user, password, server, port should correspond to a user that can access projects on a teamwork server
	1. serviceport is the port where MDServer should listen on (this is the port you input into docweb's server setup)
	1. docweb should have the address where MDServer should upload the outpus (url where docweb is hosted + "/upload/")
1. Start the Magicdraw installation normally, with a display, to dismiss all popups/license dialogs, etc and to configure it as suits your environment.

# build and test
1. there's a build.xml ant file, open it and modify the "md" property to point to your Magicdraw installation
1. run the ant file, default should build everything
1. cd into build/dist
1. run the following to start up the server, on a machine with a display, you should see the java icon pop up and when getting a request from docweb and opening a project, you should see the open project progress bar pop up
		
		./mdserver.sh config.properties OR
		
		./mdserver.sh config.properties > LOG & (this will put the output into a log file and run it in background)
		
1. look at the readme for DocWeb for integrating with the web app

# rest interface
* see gov.nasa.jpl.mbee.mdserver.ServerResource
1. PUT /request/{ticket}
	* takes in form fields project (String), package (String), user (String), web2 (int)
	* returns ticket if request successfully added to queue, "Full" if not
1. GET /queuelist
	* returns json array of the current request tickets in the queue
1. DELETE /request/{ticket}
	* returns 1 if successfully removed request from queue ( if it hasn't started yet), 0 otherwise
1. GET /projects
	* returns json object of the connect teamwork's categories and their projects
		
		{category name: [project name, ...], ...}