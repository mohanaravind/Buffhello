/**
 * 
 */
package com.nimbus.buffhello;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Aravind
 *
 */
@SuppressWarnings("serial")
public class UsersDisconnectedServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		//Log the user off
		Utility.logOffUser(req);
		
		//Push the message to the users
		Utility.pushAvailableUsersToClient(req);
		
	}
	
}
