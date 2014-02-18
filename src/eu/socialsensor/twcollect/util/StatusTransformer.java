package eu.socialsensor.twcollect.util;

import java.util.Map;

import twitter4j.Status;

public interface StatusTransformer {

	public String extractLine(Status status);
	
	public Map<String, String> extractKeyValues(Status status);
	
}
