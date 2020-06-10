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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  // Create Datastore instance to interact with the Datastore
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /** Retrieves and outputs JSON based on all user comments */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Create Query instance for Comment entities and sort by most recent comment first
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    int numberOfComments = getNumOfComments(request);
    if (numberOfComments == -1) {
      // Return empty JSON response
      response.setContentType("application/json");
      response.getWriter().println("");
    }

    // Contains a specific number of entities of type Comment in the Datastore 
    List<Entity> results = datastore.prepare(query)
        .asList(FetchOptions.Builder.withLimit(numberOfComments));

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results) {
      // Retrieve stored values from datastore
      String name = (String) entity.getProperty("name");
      String email = (String) entity.getProperty("email");
      String text = (String) entity.getProperty("text");
      float score = (float) entity.getProperty("score");

      // Create comment object with the inputted values in each field
      Comment comment = new Comment(name, email, text, score);
      
      // Store comment in data structure
      comments.add(comment);
    }

    // Convert list of comments stored in the Datastore to JSON using Gson
    response.setContentType("application/json");
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

    // Calculate sentiment score of user-inputted comment
    Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();

    // Create datastore entity for each comment
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("email", email);
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("score", score);

    // Store entity in Datastore
    datastore.put(commentEntity);

    // Redirect user back to portfolio home page form section
    response.sendRedirect("/index.html#form");
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

  /** Returns user selected number of comments, or -1 if the choice was invalid. */
  private int getNumOfComments(HttpServletRequest request) {
    // Retrieve number of comments selected by the user
    String numOfCommentsString = request.getParameter("num-comments");

    // Convert the number of comments to an int
    int numOfComments;
    try {
      numOfComments = Integer.parseInt(numOfCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numOfCommentsString);
      return -1;
    }

    // Ensure that the number of comments is positive
    if (numOfComments < 0) {
      System.err.println("Number of comments must be positive: " + numOfCommentsString);
      return -1;
    }

    return numOfComments;
  }
}