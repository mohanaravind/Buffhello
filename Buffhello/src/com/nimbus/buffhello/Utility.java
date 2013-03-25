/**
 * 
 */
package com.nimbus.buffhello;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * @author Aravind
 *
 */
public class Utility {

	public static String prepareMessageForClient(HashMap<String, String> hmLoggedInUsers, String strMessageContent, String strSentFrom){
		//Declarations
		StringBuilder sbMessage = new StringBuilder();
		int intUsersCount;
		
		//Get the count
		intUsersCount = hmLoggedInUsers.size();
		
		//Start the JSON message
		sbMessage.append("'{");
		
		//Prepare the list of logged in users
		sbMessage.append("\"AvailableUsers\":");
		sbMessage.append("[");

		for (int intIndex = 0; intIndex < intUsersCount; intIndex++) {
			sbMessage.append("\"");
			sbMessage.append(hmLoggedInUsers.keySet().toArray()[intIndex]);
			sbMessage.append("\"");
			
			//If there are still more users
			if(intIndex < intUsersCount -1)
				sbMessage.append(",");
		}
		
		sbMessage.append("]");
		sbMessage.append(",");
		
		//Prepare the message content
		sbMessage.append("\"MessageContent\":");
		sbMessage.append("\"");
		sbMessage.append(strMessageContent);
		sbMessage.append("\"");
		sbMessage.append(",");
		
		//Prepare the sent from
		sbMessage.append("\"SentFrom\":");
		sbMessage.append("\"");
		sbMessage.append(strSentFrom);
		sbMessage.append("\"");	
				
		//End of JSON
		sbMessage.append("}'");
		
		return sbMessage.toString();
	}

	/**
	 * Pushes the list of available users to the client who are currently connected
	 * @param req
	 */
	public static void pushAvailableUsersToClient(HttpServletRequest req){
		//Declarations
		DBUtility objDBUtility = new DBUtility();
		HashMap<String, String> hmLoggedInUsers;
		String strMessage = "List of online users from server";
		//Get the channel service
		ChannelService channelService = ChannelServiceFactory.getChannelService();						
		//Get the logged in users
		hmLoggedInUsers = objDBUtility.getLoggedInUsers();
						
		//Build the message
		strMessage = Utility.prepareMessageForClient(hmLoggedInUsers, strMessage, "Server");
		
		for (String strUser : hmLoggedInUsers.keySet()) {
			//Send the message to client
			channelService.sendMessage(new ChannelMessage(strUser, strMessage));
		}

	}
	
	/**
	 * Returns whether the time has expired for the token.
	 * It checks whether it has been over two hours when the token was created
	 * @param strTokenCreationTime
	 * @return
	 */
	public static boolean isTokenExpired(String strTokenCreationTime){
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMMMM.dd GGG hh:mm aaa");
	    Date dt = cal.getTime();
	    long timeExpired = 0;					    
	    
	    try {	    	
	    	timeExpired = (dt.getTime() - sdf.parse(strTokenCreationTime).getTime())/1000;		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    //If its almost 2 hours 
	    return (timeExpired > 7100);	    	    
	}
	
	
	//Log off the user from session
	public static void logOffUser(HttpServletRequest req){
		//Declarations
		Key userDataKey;
		Entity userData;		
		
		//Get the data store
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		ChannelPresence presence;
		try {
			presence = channelService.parsePresence(req);
			
			//Create the user data key
			userDataKey = KeyFactory.createKey("ConnectedUsers", presence.clientId());		
			
			//Create the entity 
			userData = new Entity(userDataKey);
			
			try {
				userData = datastore.get(userDataKey);
				
				//Set the logged off status
				userData.setProperty("IsAvailable", false);
				
				//Update to data store
				datastore.put(userData);
				
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				

	}

	
	public static void logInUser(String strToken){		
		//Get the data store
		Key userDataKey;
		Entity userData;
		String strUser;
		
		//Get the logged in user detail
		UserService objUserService = UserServiceFactory.getUserService();
		User objUser = objUserService.getCurrentUser();
		strUser = objUser.getNickname();
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		//Create the user data key
		userDataKey = KeyFactory.createKey("ConnectedUsers", strUser);		
		
		//Create the entity 
		userData = new Entity(userDataKey);
		
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMMMM.dd GGG hh:mm aaa");
	    				
		userData.setProperty("User", strUser);		
		userData.setProperty("IsAvailable", true);		
		userData.setProperty("Token", strToken);
		userData.setProperty("LoggedInAt", sdf.format(cal.getTime()));

		//Update the user detail
		datastore.put(userData);
	}
	
}
