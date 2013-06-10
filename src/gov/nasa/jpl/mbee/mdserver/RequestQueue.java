package gov.nasa.jpl.mbee.mdserver;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestQueue extends LinkedBlockingQueue<Request> {
	
	private final static RequestQueue instance = new RequestQueue();
	
	private RequestQueue() {
		super();
	}
	
	public static RequestQueue getInstance() {
		return instance;
	}
}
