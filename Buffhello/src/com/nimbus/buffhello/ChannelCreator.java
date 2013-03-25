/**
 * 
 */
package com.nimbus.buffhello;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


/**
 * @author Aravind
 *
 */
public class ChannelCreator {
	private List<String> lstUsers;

	private HashMap<String, String> hmUserAvailability;
	private HashMap<String, String> hmLoggedInAt;
	private HashMap<String, String> hmUserTokens;

	public ChannelCreator(){
		//Initialize
		lstUsers = new ArrayList<String>();
		hmUserAvailability = new HashMap<String, String>();
		hmLoggedInAt = new HashMap<String, String>();
		hmUserTokens = new HashMap<String, String>();
	}


	/**
	 * Creates the channel communication for the logged in user
	 */	
	public String getToken(){
		//Declarations
		String strUser;
		String strToken = "";

		//Get the logged in user detail
		UserService objUserService = UserServiceFactory.getUserService();
		User objUser = objUserService.getCurrentUser();

		//If there is a logged in user
		if(objUser != null){
			strUser = objUser.getNickname();		

			//validates the user connections and creates the required channels
			validateUserConnection(strUser);

			//Get the token
			strToken = "'" + hmUserTokens.get(strUser) + "'";
		}

		return strToken;		
	}

	/**
	 * Checks whether the user is connected or not
	 * Returns true if the user has already established a connection
	 * @return Boolean
	 */
	private boolean validateUserConnection(String strUser){
		//Declarations
		boolean blnIsUserConnected = false;		
		boolean blnIsTokenExpired = true;		
		Key userDataKey;
		Entity userData;
		String strToken;
		String strUserRecord;

		//Get the data store
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Create the user data key
		userDataKey = KeyFactory.createKey("ConnectedUsers", strUser);		
		
		//Create the entity 
		userData = new Entity(userDataKey);
		
		//Get the users data from data store
		try {

			//Get the list of logged in users from the buffHello data						
			Query query = new Query("ConnectedUsers", userDataKey);
			List<Entity> lstUsersData = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));

			//Get the data
			for(Entity objUserData : lstUsersData){
				//Get the user record
				strUserRecord = objUserData.getProperty("User").toString();
				
				//Construct the data structures												
				hmUserAvailability.put(strUserRecord, objUserData.getProperty("IsAvailable").toString());
				
				//If the user is currently available
				if((Boolean) objUserData.getProperty("IsAvailable"))
					lstUsers.add(strUserRecord);
											
				hmUserTokens.put(strUserRecord, objUserData.getProperty("Token").toString());
				hmLoggedInAt.put(strUserRecord, objUserData.getProperty("LoggedInAt").toString());								
			}			

			//Get the connection status of the user
			blnIsUserConnected = hmUserTokens.containsKey(strUser);
			
			//Get the token validity
			if(hmLoggedInAt.containsKey(strUser))
				blnIsTokenExpired = Utility.isTokenExpired(hmLoggedInAt.get(strUser));
						
			//If the user is not logged in yet
			if(!lstUsers.contains(strUser)){
				Utility.logInUser(hmUserTokens.get(strUser));
				lstUsers.add(strUser);
			}
			
			//Remove the user entry if the token has expired
			if(blnIsTokenExpired){
				//Get the user data
				datastore.delete(userDataKey);
			}
									
			//If the user is not yet connected or the token has expired
			if(!blnIsUserConnected || blnIsTokenExpired){
				//Create a channel for this user
				strToken = createChannel(strUser);

				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMMMM.dd GGG hh:mm aaa");
			    				
				userData.setProperty("User", strUser);
				userData.setProperty("Token", strToken);
				userData.setProperty("IsAvailable", true);								
				userData.setProperty("LoggedInAt", sdf.format(cal.getTime()));

				//Update the token map
				hmUserTokens.put(strUser, strToken);
				
				if(!lstUsers.contains(strUser))
					lstUsers.add(strUser);

				datastore.put(userData);
			}

		} catch (Exception e) {
			//Log exception

		}


		return blnIsUserConnected;
	}


	/**
	 * Creates a channel for the given user
	 * @param strUser
	 * @return
	 */
	private String createChannel(String strUser){
		ChannelService channelService = ChannelServiceFactory.getChannelService();

		//Return the created token
		return channelService.createChannel(strUser);		
	}

	




}
