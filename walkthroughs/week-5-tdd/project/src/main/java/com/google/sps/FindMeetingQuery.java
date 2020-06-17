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

    // Try finding a suitable meeting time for all attendees and if no time exists, priortize required attendees
    Collection<String> allAttendees = new ArrayList<String>() {{
      addAll(requiredAttendees);
      addAll(optionalAttendees);
    }};
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
        TimeRange currentEvent = eventsSortedByStartTime.get(i);
        TimeRange nextEvent = eventsSortedByStartTime.get(i + 1);
        for (i = 0; i < numberOfEvents - 1; i++) { 
          if (!currentEvent.overlaps(nextEvent)) {
            if (currentEvent.end() < nextEvent.start() && nextEvent.start() - currentEvent.end() >= meetingDuration) {
              meetingQueryOptions.add(TimeRange.fromStartEnd(currentEvent.end(), nextEvent.start(), /* inclusive= */ false));
            }
            currentEvent = eventsSortedByStartTime.get(i);
          }
          nextEvent = eventsSortedByStartTime.get(i + 1);
        }
      }

      if (TimeRange.END_OF_DAY - lastEventEndTime >= meetingDuration) {
        meetingQueryOptions.add(TimeRange.fromStartEnd(lastEventEndTime, TimeRange.END_OF_DAY, true));
      }
    }
    return meetingQueryOptions;
  }
}
