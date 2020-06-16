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
    Collection<String> requiredAttendees = request.getAttendees();
    long meetingDuration = request.getDuration();
    List<TimeRange> meetingQueryOptions = new ArrayList<>();

    if (meetingDuration > TimeRange.WHOLE_DAY.duration() || meetingDuration <= 0) {
      return meetingQueryOptions;
    }
    
    List<TimeRange> eventsSortedByStartTime = new ArrayList<>();

    for (Event event : events) {
      // Add event to events lists if meeting request contains attendees of the event  
      if (!Collections.disjoint(requiredAttendees, event.getAttendees())) {
        eventsSortedByStartTime.add(event.getWhen());
      }
    }
    Collections.sort(eventsSortedByStartTime, TimeRange.ORDER_BY_START);

    if (requiredAttendees.isEmpty() || eventsSortedByStartTime.isEmpty()) {
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
        for (int currentEventIndex = 0; currentEventIndex < numberOfEvents - 1; currentEventIndex++) {
          int currentEventEndTime = eventsSortedByStartTime.get(currentEventIndex).end();
          int nextEventStartTime = eventsSortedByStartTime.get(currentEventIndex+1).start();
          if (currentEventEndTime < nextEventStartTime && nextEventStartTime - currentEventEndTime >= meetingDuration) {
            meetingQueryOptions.add(TimeRange.fromStartEnd(currentEventEndTime, nextEventStartTime, /* inclusive= */ false));
          }
        }
      }

      if (TimeRange.END_OF_DAY - lastEventEndTime >= meetingDuration) {
        meetingQueryOptions.add(TimeRange.fromStartEnd(lastEventEndTime, TimeRange.END_OF_DAY, true));
      }
    }
    return meetingQueryOptions;
  }
}
