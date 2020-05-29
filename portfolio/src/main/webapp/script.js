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

const facts = [
    "I have lived in three countries: India, Canada, and the US!", 
    "I can speak/understand 4 different languages!", 
    "I love eating Chocolate Chip Cookie Dough ice cream!", 
    "I have only visited 8 of the 50 US states!"
];

/**
 * Adds a random fun fact to the page.
 */
function addRandomFunFact() {
  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

var currentProjIndex = 0;
var projects = ["images/proj1.png", "images/proj2.png", "images/proj3.png", "images/proj4.png"];
var changeProjectTimeMs = 8000;

window.setInterval(function(){
  changeProject();
}, changeProjectTimeMs);

function changeProject() {
    if (currentProjIndex < projects.length-1) {
        currentProjIndex++;
    } else {
        currentProjIndex = 0;
    }
    document.getElementById("currProj").src = projects[currentProjIndex];
}