package com.jobportal.application.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

    @Value("${google.calendar.service-account-json}")
    private Resource serviceAccountJson;

    @Value("${google.calendar.id}")
    private String calendarId;

    private Calendar buildCalendar() throws Exception {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(serviceAccountJson.getInputStream())
                .createScoped(List.of(CalendarScopes.CALENDAR));
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("HireHub")
                .build();
    }

    public CalendarResult createEvent(String title, LocalDateTime start, LocalDateTime end,
                                      String candidateEmail, String recruiterEmail) {
        try {
            Calendar service = buildCalendar();

            Event event = new Event()
                    .setSummary(title)
                    .setDescription("Interview scheduled via HireHub");

            ZoneId zone = ZoneId.systemDefault();
            event.setStart(new EventDateTime()
                    .setDateTime(new DateTime(start.atZone(zone).toInstant().toEpochMilli()))
                    .setTimeZone(zone.getId()));
            event.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(end.atZone(zone).toInstant().toEpochMilli()))
                    .setTimeZone(zone.getId()));

            event.setAttendees(List.of(
                    new EventAttendee().setEmail(candidateEmail),
                    new EventAttendee().setEmail(recruiterEmail)));

            // Request Google Meet conference link
            event.setConferenceData(new ConferenceData()
                    .setCreateRequest(new CreateConferenceRequest()
                            .setRequestId(UUID.randomUUID().toString())
                            .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"))));

            Event created = service.events().insert(calendarId, event)
                    .setConferenceDataVersion(1)
                    .setSendUpdates("all")
                    .execute();

            String meetLink = null;
            if (created.getConferenceData() != null && created.getConferenceData().getEntryPoints() != null) {
                meetLink = created.getConferenceData().getEntryPoints().stream()
                        .filter(e -> "video".equals(e.getEntryPointType()))
                        .map(EntryPoint::getUri)
                        .findFirst().orElse(null);
            }

            log.info("Google Calendar event created: {}", created.getId());
            return new CalendarResult(created.getId(), meetLink);

        } catch (Exception e) {
            log.error("Failed to create Google Calendar event: {}", e.getMessage());
            return new CalendarResult(null, null);
        }
    }

    public void deleteEvent(String eventId) {
        if (eventId == null) return;
        try {
            buildCalendar().events().delete(calendarId, eventId).execute();
            log.info("Google Calendar event deleted: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event {}: {}", eventId, e.getMessage());
        }
    }

    public record CalendarResult(String eventId, String meetLink) {}
}
