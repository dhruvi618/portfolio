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

function onWindowLoad() {
  changeProject();
  getNumCommentsSelectedAndDisplay();
  getUserLoginStatus();
}

const facts = [
  "I have lived in three countries: India, Canada, and the US!", 
  "I can speak/understand 4 different languages!", 
  "I love eating Chocolate Chip Cookie Dough ice cream!", 
  "I have only visited 8 of the 50 US states!"
];

/** Adds a random fun fact to the page */
function addRandomFunFact() {
  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

var currentProjIndex = 0;
var projects = ["images/proj1.png", "images/proj2.png", "images/proj3.png", "images/proj4.png"];
const changeProjectTimeMs = 5000;
var changeCurrProj;

/** Advances to the next project slide */
function changeProject() {
  if (currentProjIndex < projects.length-1) {
    currentProjIndex++;
  } else {
    currentProjIndex = 0;
  }
  document.getElementById("currProj").src = projects[currentProjIndex];
  changeCurrProj = setTimeout("changeProject()", changeProjectTimeMs);
}

/** Pauses project slideshow on mouse hover */
function pauseProjectChange() {
  clearTimeout(changeCurrProj);
}

google.charts.load('current', {'packages':['timeline']});
google.charts.setOnLoadCallback(drawChart);

/** 
 * Fetches programming experience data and draws chart. 
 * In the case of incorrectly formatted data, display error to user
 */
function drawChart() {
  fetch('/programming-data').then(response => response.json()).then((programmingData) => {
    const data = new google.visualization.DataTable();
    data.addColumn({ type: 'string', id: 'Programming Language' });
    data.addColumn({ type: 'date', id: 'Start' });
    data.addColumn({ type: 'date', id: 'End' });

    // Parse fetched data and add entry of format ["Programming Language", "Start Date", "End Date"] to array
    const rowData = [];
    programmingData.forEach((programmingEntry) => {
      rowData.push([programmingEntry.programmingLanguage, new Date(programmingEntry.startDate),
          new Date(programmingEntry.endDate)]);
    });
    data.addRows(rowData);

    // Initialize and draw the timeline chart based on view options
    const chart = new google.visualization.Timeline(document.getElementById('chart-container'));
    const options = {
      title: 'Programing Language Experiences',
      width: '100%', 
      height: '100%',
    };
    chart.draw(data, options);
  });
}

/** Fetches comment(s) and updates the UI to display them */
function fetchCommentAndDisplay(numOfCommentsToDisplay) {
  // Clear out old comments before inserting new comments into the DOM
  const commentsContainer = document.getElementById('comments-container');
  commentsContainer.innerHTML = "";

  fetch('/data?num-comments=' + numOfCommentsToDisplay).then(response => response.json()).then((comments) => {
    comments.forEach((comment) => {
      const currentCommentContainer = createDivElement();
      currentCommentContainer.style.padding = "50px 0px";
      commentsContainer.appendChild(currentCommentContainer);
      currentCommentContainer.appendChild(createParagraphElement(comment.name));
      currentCommentContainer.appendChild(createParagraphElement(comment.email));
      currentCommentContainer.appendChild(createParagraphElement(comment.text));
      currentCommentContainer.appendChild(createParagraphElement(comment.score));
    });
  });
}

function getNumCommentsSelectedAndDisplay() {
  // Retrieve number of comments selected by the user
  const selectElement = document.getElementById('num-comments');
  const numOfCommentsToDisplay = selectElement.options[selectElement.selectedIndex].value;

  // Call to update UI
  fetchCommentAndDisplay(numOfCommentsToDisplay);
}

/** Creates an <p> element containing text. */
function createParagraphElement(text) {
  const paragraphElement = document.createElement('p');
  paragraphElement.innerText = text;
  return paragraphElement;
}

/** Creates an <div> element containing comment object. */
function createDivElement() {
  return document.createElement('div');
}

/** Fetch and post to servlet to delete all comment entries and update UI */
function deleteCommentsAndUpdateDisplay() {
  fetch("/delete-data", { method: 'POST' }).then(function(response) {
    // Check if response code is 200 which signifies that the deletion was successful
    if (response.status === 200) {
      // Update UI
      fetchCommentAndDisplay(0);
    } else {
      // Notify user that comments could not be deleted
      alert("Error deleting comments from datastore.");
    }
  });
}

/** Get login status of the user */
function getUserLoginStatus() {
  fetch('/login').then(response => response.json()).then((message) => {
    // Get login status from JSON response
    const loginStatus = message.loginStatus;

    // Create link element to display login/logout URL to user
    const linkElement = document.createElement('a');
    linkElement.href = message.url;

    const loginContainer = document.getElementById('login-container');
    loginContainer.appendChild(linkElement);

    // Add login/logout link and show/hide comments based on login status
    linkElement.innerText = loginStatus ? "Logout" : "Login";
    document.getElementById('comments-container').style.display = loginStatus ? 'block' : 'none';
  });
}