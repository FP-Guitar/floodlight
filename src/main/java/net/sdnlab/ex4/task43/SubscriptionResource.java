package net.sdnlab.ex4.task43;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sdnlab.ex4.task43.Subscription.OPERATOR;
import net.sdnlab.ex4.task43.Subscription.TYPE;

import static net.sdnlab.common.Helper.simpleStatusMessage;
public class SubscriptionResource extends ServerResource {

	@SuppressWarnings("unchecked")
	@Post
	public String postSubscription(String json) {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		String name = (String) getRequestAttributes().get("name");		

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			HashMap<String,String> requestEntries = new HashMap<String, String>();
			requestEntries = objectMapper.readValue(json,  HashMap.class);
			// some basic check if the json is complete
			if ( ! checkIfComplete(requestEntries) ) {
				return simpleStatusMessage("Entry is missing, check if one is missing/typocheck!: " + neededEntries());
			}
			// try to get entries, failures are handled completly by exceptions
			int port = Integer.parseInt( requestEntries.get("port") );
			int reference_value = Integer.parseInt( requestEntries.get("reference_value") );
			IPv4Address ipAddress = IPv4Address.of( requestEntries.get("ipv4_dst") );
			Subscription.OPERATOR operator = OPERATOR.fromString( requestEntries.get("operator") );
			Subscription.TYPE filterType = TYPE.fromString(requestEntries.get("filter_type"));
			
			Subscription subscription = new Subscription(name, ipAddress, port, filterType, operator, reference_value );
			
			return task43Service.addSubscription(subscription);		
		} catch (JsonParseException e) {
			return simpleStatusMessage("Failure parsing Json: " + e.getMessage()); 	
		} catch (JsonMappingException e) {
			return simpleStatusMessage("Failure parsing Json: " + e.getMessage());
		} catch (IOException e) {
			return simpleStatusMessage("Failure parsing Json: " + e.getMessage());
		} catch (NumberFormatException e ) {
			return simpleStatusMessage("Failure parsing Json: expected an integervalue:" + e.getMessage() );
		} catch (IllegalArgumentException e) {
			return simpleStatusMessage("Illegal Argument: " + e.getMessage());
		}
	}

	@Delete
	public String deleteSubscription() {
		ITask43Service task43Service = (ITask43Service) getContext().getAttributes()
				.get(ITask43Service.class.getCanonicalName());
		String name = (String) getRequestAttributes().get("name");
		return task43Service.deleteSubscription(name);
	}

	
	private boolean checkIfComplete( Map<String,String> map ) {
		boolean isComplete = true;
		isComplete = isComplete && map.containsKey("ipv4_dst");
		isComplete = isComplete && map.containsKey("port");
		isComplete = isComplete && map.containsKey("operator");
		isComplete = isComplete && map.containsKey("reference_value");
		isComplete = isComplete && map.containsKey("filter_type");
		return isComplete;
	}
	
	private String neededEntries() {
		return "ipv4_dst, port, operator, reference_value";
	}
}
