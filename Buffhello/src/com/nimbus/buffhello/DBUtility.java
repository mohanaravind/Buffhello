/**
 * 
 */
package com.nimbus.buffhello;

import java.util.HashMap;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

/**
 * @author Aravind
 *
 */
public class DBUtility {

	public HashMap<String, String> getLoggedInUsers(){
		//Declarations
		HashMap<String, String> hmLoggedInUsers = new HashMap<String, String>();		
		
		//Get the data store
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


		//Get the users data from data store
		try {

			//Get the list of logged in users from the buffHello data						
			Query query = new Query("ConnectedUsers");
			List<Entity> lstUsersData = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(100));

			//Get the list of logged in users
			for(Entity objUserData : lstUsersData){				
				//Check whether the user is logged in
				if((Boolean) objUserData.getProperty("IsAvailable"))
					hmLoggedInUsers.put(objUserData.getProperty("User").toString(), objUserData.getProperty("Token").toString());
			}			

		} catch (Exception e) {
			//Log exception

		}

		return hmLoggedInUsers;
	}
	
}
