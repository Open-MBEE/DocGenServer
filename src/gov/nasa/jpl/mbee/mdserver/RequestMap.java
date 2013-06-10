package gov.nasa.jpl.mbee.mdserver;

import java.util.concurrent.ConcurrentHashMap;

public class RequestMap extends ConcurrentHashMap<String, Request> {
	private final static RequestMap instance = new RequestMap();
	
	private RequestMap() {
		super();
	}
	
	public static RequestMap getInstance() {
		return instance;
	}
}
