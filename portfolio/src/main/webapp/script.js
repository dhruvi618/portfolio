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
  fetchCommentAndDisplay(0);
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

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Creates a chart and adds it to the page. */
function drawChart() {
  // Define data values for chart in format [Programming Language, Users]
  var data = google.visualization.arrayToDataTable([
    ['Programming Languages', 'Users'],
    ['Java',  900],
    ['JavaScript',  1000],
    ['Python',  1170],
    ['C/C++',  1250],
    ['C#',  1530]
  ]);

  // Define title and view options for the chart
  const options = {
    title: 'Trending Programming Languages',
    width: 500,
    height: 500,
    backgroundColor: '#111',
    hAxis: {
      textStyle: {
        color: '#FFF'
      }
    },
    vAxis: {
      textStyle: {
        color: '#ffffff'
      }
    },
    legend: {
      textStyle: {
        color: '#ffffff'
      }
    },
    titleTextStyle: {
      color: '#ffffff'
    }
  };

  // Initialize and draw the bar chart
  const chart = new google.visualization.BarChart(document.getElementById('chart-container'));
  chart.draw(data, options);
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
      // Change selected option to 0 since the Datastore has been emptied
      document.getElementById('num-comments').selectedIndex = 0;
      
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

    // Create link element and set its reference to the JSON response URL 
    const linkElement = document.createElement('a');
    const url = message.url;
    linkElement.href = url;

    // Create paragraph element and append link to it
    const paragraphElement = createParagraphElement("Click to ");
    paragraphElement.appendChild(linkElement);

    // Get login container
    const loginContainer = document.getElementById('login-container');

    // Add login/logout link to page based on login status
    if (loginStatus === true) {
      // Set link element to logout and add to paragraph element
      linkElement.innerText = "logout";
      loginContainer.appendChild(paragraphElement);
    } else {
      // Set link element to login and add to paragraph element
      linkElement.innerText = "login";
      loginContainer.appendChild(paragraphElement);
    }
  });
}