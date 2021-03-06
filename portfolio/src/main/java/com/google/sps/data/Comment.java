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

/**
 * Class representing a comment, where each comment contains a name, email, and text
 *
 * Note: The private variables in this class are converted into JSON.
 */
public class Comment {

  private String name;
  private String email;
  private String text;
  
  /** 
   * Sentiment score between -1.0 (negative sentiment) and 1.0 (positive sentiment)
   */
  private double score;

  public Comment(String name, String email, String text, double score) {
    this.name = name;
    this.email = email;
    this.text = text;
    this.score = score;
  }
}