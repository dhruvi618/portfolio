// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Login;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles user logins */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Set response type to JSON
    response.setContentType("application/json");

    // Gets reference to a UserService
    UserService userService = UserServiceFactory.getUserService();

    // Checks and modify behavior based on status of user log in 
    if (userService.isUserLoggedIn()) {
      // Gets email of current user
      String userEmail = userService.getCurrentUser().getEmail();

      // Set redirect URL to form section of portfolio page
      String urlToRedirectToAfterUserLogsOut = "/";
      
      // Creates a logout URL
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

      // Create login object based on current login status, email, and logout url
      Login login = new Login(true, userEmail, logoutUrl);

      // Create and send JSON response using Gson
      String json = new Gson().toJson(login);
      response.getWriter().println(json);
    } else {
      // Set redirect URL
      String urlToRedirectToAfterUserLogsIn = "/";

      // Creates a login URL
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      // Create login object based on current login status, email, and login url
      Login login = new Login(false, null, loginUrl);

      // Create and send JSON response using Gson
      String json = new Gson().toJson(login);
      response.getWriter().println(json);
    }
  }
}
