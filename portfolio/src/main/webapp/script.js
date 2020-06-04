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

/** Fetches comment(s) and updates the UI to display them */
function fetchCommentAndDisplay(numOfCommentsToDisplay) {
  fetch('/data').then(response => response.json()).then((comments) => {
    /** 
    * Error check to ensure the user did not select to display more comments than exist in the datastore
    *
    * UI is not updated on user selecting to view more comments than exist in the datastore
    */ 
    if (numOfCommentsToDisplay > comments.length) {
      alert("The selected value is greater than the total number of comments.");
    }
    else {

      // Clear out old comments before inserting new comments into the DOM
      const commentsContainer = document.getElementById('comments-container');
      commentsContainer.innerHTML = "";
        
      // Display selected number of comments on the page
      var i = 0;
      while (i < numOfCommentsToDisplay) {
        let currentComment = comments[i];
        const currentCommentContainer = createDivElement();
        currentCommentContainer.style.padding = "50px 0px";
        commentsContainer.appendChild(currentCommentContainer);
        currentCommentContainer.appendChild(createParagraphElement(currentComment.name));
        currentCommentContainer.appendChild(createParagraphElement(currentComment.email));
        currentCommentContainer.appendChild(createParagraphElement(currentComment.text));
        i++;
      }
    }
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

/** Asynchronously fetch and post to servlet to delete all comment entries and update UI */
async function deleteCommentsAndUpdateDisplay() {
  const response = await fetch("/delete-data", { method: 'POST' });
  const jsonResponse = await response.json();

  // Check if response is empty which signifies that the deletion was successful
  if (jsonResponse === "") {

    // Change selected option to 0 since the Datastore has been emptied
    document.getElementById('num-comments').selectedIndex = 0;

    fetchCommentAndDisplay(0);
  }
  else {
    alert("Error deleting comments from datastore.");
  }
}