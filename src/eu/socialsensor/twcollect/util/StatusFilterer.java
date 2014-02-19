package eu.socialsensor.twcollect.util;

import twitter4j.Status;

public interface StatusFilterer {

	public boolean acceptStatus(Status status);
	
}
