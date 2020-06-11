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

import com.google.gson.Gson;
import com.google.sps.data.ProgrammingExperience;
import java.io.IOException;
import java.lang.Integer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Returns programming language experience data as a JSON object */
@WebServlet("/programming-data")
public class ProgrammingExperienceServlet extends HttpServlet {

  private List<ProgrammingExperience> programmingExperiences = new ArrayList<>();
  boolean parseError = false;

  // Parse CSV file data and compute list of programming experience. On error, stop parsing and return to caller
  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(
        "/WEB-INF/programming-language-experience.csv"));
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String programmingLanguage = String.valueOf(cells[0]);
      String startDateString = String.valueOf(cells[1]);
      String endDateString = String.valueOf(cells[2]);

      // Parse dates using mm/dd/yyyy pattern and create corresponding Date objects
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
      formatter.setLenient(false);
      try {  
        Date startDate = formatter.parse(startDateString);
        Date endDate = formatter.parse(endDateString);
        ProgrammingExperience experience = new ProgrammingExperience(programmingLanguage, startDate, endDate);
        programmingExperiences.add(experience);
      } catch (ParseException e) {
        e.printStackTrace();
        parseError = true;
        break;
      }
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Send error code 500 in event of parsing exception and programming experiences JSON on success
    if (parseError) {
      response.setStatus(500);
    } else {
      response.setContentType("application/json");
      String json = new Gson().toJson(programmingExperiences);
      response.getWriter().println(json);
    } 
  }
}