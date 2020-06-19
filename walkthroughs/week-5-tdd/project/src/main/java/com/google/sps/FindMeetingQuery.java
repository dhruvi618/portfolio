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

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> meetingOptions = new ArrayList<>();
    long meetingDuration = request.getDuration();

    if (meetingDuration > TimeRange.WHOLE_DAY.duration() || meetingDuration <= 0) {
      return meetingOptions;
    }
    
    Collection<String> requiredAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    if (requiredAttendees.isEmpty()) {
      return findMeetingTime(optionalAttendees, events, meetingDuration);
    }
    if (optionalAttendees.isEmpty()) {
      return findMeetingTime(requiredAttendees, events, meetingDuration);
    }

    List<String> allAttendees = new ArrayList<>();
    allAttendees.addAll(requiredAttendees);
    allAttendees.addAll(optionalAttendees);

    // Try finding a suitable meeting time for all attendees and if no time exists, priortize required attendees
    meetingOptions = findMeetingTime(allAttendees, events, meetingDuration);
    if (meetingOptions.isEmpty()) {
      return findMeetingTime(requiredAttendees, events, meetingDuration);
    }
    return meetingOptions;
  }

  public List<TimeRange> findMeetingTime(Collection<String> attendees, Collection<Event> events, long meetingDuration) {
    List<TimeRange> meetingQueryOptions = new ArrayList<>();
    List<TimeRange> eventsSortedByStartTime = new ArrayList<>();

    for (Event event : events) {
      // Add event to events lists if meeting request contains attendees of the event  
      if (!Collections.disjoint(attendees, event.getAttendees())) {
        eventsSortedByStartTime.add(event.getWhen());
      }
    }
    Collections.sort(eventsSortedByStartTime, TimeRange.ORDER_BY_START);

    if (attendees.isEmpty() || eventsSortedByStartTime.isEmpty()) {
      meetingQueryOptions.add(TimeRange.WHOLE_DAY);
    } else {
      int numberOfEvents = eventsSortedByStartTime.size();
      int firstEventStartTime = eventsSortedByStartTime.get(0).start();
      TimeRange lastEventRangeByEndTime = Collections.max(eventsSortedByStartTime, Comparator.comparingInt(TimeRange::end));
      int lastEventEndTime = lastEventRangeByEndTime.end();

      if (firstEventStartTime - TimeRange.START_OF_DAY >= meetingDuration) {
        meetingQueryOptions.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstEventStartTime, false));
      }

      if (numberOfEvents > 1) {
        int i = 0;
        // Loop over events and add meeting option if it satisfies conditions.
        while (i < numberOfEvents - 1) {
          TimeRange currentEvent = eventsSortedByStartTime.get(i);
          TimeRange nextEvent = eventsSortedByStartTime.get(i + 1);

          // Add time to meeting options if the current event ends prior to the next event starting 
          // and there is adequate time for a meeting based on request. 
          if (currentEvent.end() < nextEvent.start() && nextEvent.start() - currentEvent.end() >= meetingDuration) {
            meetingQueryOptions.add(TimeRange.fromStartEnd(currentEvent.end(), nextEvent.start(), /* inclusive= */ false));
          } else if (currentEvent.end() > nextEvent.start()) {
            // If the next event does not end before the current event, skip over the next event on next iteration 
            // of loop since no meeting is possible in that time range.
            // (i.e. Event 1: 9:00am-2:00pm, Event 2: 9:30am-1:00pm, Event 3: 3:00pm-5:00pm) skip over Event 2 
            i++;
          }
          i++;
        }
      }

      if (TimeRange.END_OF_DAY - lastEventEndTime >= meetingDuration) {
        meetingQueryOptions.add(TimeRange.fromStartEnd(lastEventEndTime, TimeRange.END_OF_DAY, true));
      }
    }
    return meetingQueryOptions;
  }
}
