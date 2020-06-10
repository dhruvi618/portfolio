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

package com.google.sps.data;

import java.util.Date;

/**
 * Class representing programming experience data, where each experience contains a language, start date, and end date
 *
 * Note: The private variables in this class are converted into JSON.
 */
public class ProgrammingExperience {

  private String programmingLanguage;
  private Date startDate;
  private Date endDate;

  public ProgrammingExperience(String programmingLanguage, Date startDate, Date endDate) {
    this.programmingLanguage = programmingLanguage;
    this.startDate = startDate;
    this.endDate = endDate;
  }
}