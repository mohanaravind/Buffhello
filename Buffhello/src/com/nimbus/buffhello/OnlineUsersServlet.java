/**
 * 
 */
package com.nimbus.buffhello;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * @author Aravind
 *
 */
@SuppressWarnings("serial")
public class OnlineUsersServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//Declarations
		DBUtility objDBUtility = new DBUtility();
		HashMap<String, String> hmLoggedInUsers;		
		String strMessage = "List of online users from server";
		
		//Get the logged in user detail
		UserService objUserService = UserServiceFactory.getUserService();
		User objUser = objUserService.getCurrentUser();		
		
		//Set the user name as the channel key
		String strChannelKey = objUser.getNickname();

		//Get the channel service to communicate with the client
		ChannelService channelService = ChannelServiceFactory.getChannelService();

		//Get the logged in users
		hmLoggedInUsers = objDBUtility.getLoggedInUsers();
		
		//Build the message
		strMessage = Utility.prepareMessageForClient(hmLoggedInUsers, strMessage, "Server");
		
		//Send the message to client
		channelService.sendMessage(new ChannelMessage(strChannelKey, strMessage));

	}
	
	
	
}
