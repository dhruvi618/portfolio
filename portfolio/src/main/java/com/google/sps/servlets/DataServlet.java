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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import com.google.sps.data.Comment;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

/** Servlet that handles comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /** Retrieves and outputs JSON based on all user comments */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create Query instance for Comment entities and sort based on time recieved 
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    // Create Datastore instance to interact with the Datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Contains all entities of type Comment in the Datastore 
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      
      // Retrieve stored values from datastore
      String name = (String) entity.getProperty("name");
      String email = (String) entity.getProperty("email");
      String text = (String) entity.getProperty("text");

      // Create comment object with the inputted values in each field
      Comment comment = new Comment(name, email, text);
      
      // Store comment in data structure
      comments.add(comment);
    }

    // Convert list of comments stored in the Datastore to JSON using Gson
    response.setContentType("application/json;");
    String json = new Gson().toJson(comments);
    response.getWriter().println(json);
  }

  /** Creates and stores a comment entity based on user-inputted values */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String name = getParameter(request, "name", "");
    String email = getParameter(request, "email", "");
    String text = getParameter(request, "text-input", "");

    // Get timestamp of when comment was submitted for sorting purposes
    long timestamp = System.currentTimeMillis();

    // Create datastore entity for each comment
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("email", email);
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("timestamp",timestamp);

    // Create datastore variable that allows for interatction with Datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Store entity in Datastore
    datastore.put(commentEntity);

    // Redirect user back to portfolio home page
    response.sendRedirect("/index.html");
  }

  /**
  * @return the request parameter, or the default value if the parameter
  *         was not specified by the client
  */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

}
