package gov.nasa.jpl.mbee.mdserver;

import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.sun.grizzly.http.SelectorThread;

public class Shutdown implements Runnable {

	public void run() {
		TeamworkUtils.logout();
		SelectorThread jersey = Config.getInstance().getSelectorThread();
		if (jersey != null) {
			jersey.stopEndpoint();
		}
	}

}
