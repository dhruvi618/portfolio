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
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data deletion */
@WebServlet("/delete-data")
public class DataDeleteServlet extends HttpServlet {

  /** Deletes all entries in datastore and outputs and empty response */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Create Datastore instance to interact with the Datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Create Query instance for Comment entities
    Query query = new Query("Comment");

    // Contains all entities of type Comment in the Datastore 
    PreparedQuery results = datastore.prepare(query);

    // Iterate through each entity in the Datastore and delete it
    for (Entity entity : results.asIterable()) {
      datastore.delete(entity.getKey());
    }

    // Return empty response
    response.getWriter().println();

    // Redirect user back to portfolio home page
    response.sendRedirect("/index.html");
  }
}