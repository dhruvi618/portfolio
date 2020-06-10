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

  private List<ProgrammingExperience> programmingExperience = new ArrayList<>();

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

      String[] startDateComponents = startDateString.split("/");
      int startMonth = Integer.parseInt(startDateComponents[0]) - 1;
      int startDay = Integer.parseInt(startDateComponents[1]);
      int startYear = Integer.parseInt(startDateComponents[2]);
      Date startDate = new Date(startYear, startMonth, startDay);

      String[] endDateComponents = endDateString.split("/");
      int endMonth = Integer.parseInt(endDateComponents[0]) - 1;
      int endDay = Integer.parseInt(endDateComponents[1]);
      int endYear = Integer.parseInt(endDateComponents[2]);
      Date endDate = new Date(endYear, endMonth, endDay);

      ProgrammingExperience programmingExperience = new ProgrammingExperience(programmingLanguage, startDate, endDate);

      programmingExperience.add(programmingExperience);
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(programmingExperience);
    response.getWriter().println(json);
  }
}
