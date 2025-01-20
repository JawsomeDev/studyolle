package com.studyolle.modules.event;


import com.studyolle.modules.account.CurrentUser;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.study.Study;
import com.studyolle.modules.event.form.EventForm;
import com.studyolle.modules.event.validator.EventValidator;
import com.studyolle.modules.study.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final EventService eventService;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(eventValidator);
    }


    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account,@PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";

    }
    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                 @Valid @ModelAttribute EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        model.addAttribute(studyService.getStudy(path));
        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(study);
        model.addAttribute(account);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach( e -> {
            if(e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            }else {
                newEvents.add(e);
            }
        });
        model.addAttribute("oldEvents", oldEvents);
        model.addAttribute("newEvents", newEvents);

        return "study/events";
     }

     @GetMapping("events/{id}/edit")
    public String editEventForm(@CurrentUser Account account,
                                @PathVariable String path,
                                @PathVariable Long id,
                                Model model) {
        Study study = studyService.getStudyToUpdate(path, account);
        Event event = eventRepository.findById(id).orElseThrow();
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));

        return "event/update-form";
     }

     @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable Long id, @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdate(path, account);
        Event event = eventRepository.findById(id).orElseThrow();
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if(errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
     }

    @PostMapping("/events/{id}/delete")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(eventRepository.findById(id).orElseThrow());
        return "redirect:/study/" + study.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account,
                                @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account,
                                   @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.checkInEnrolment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentUser Account account, @PathVariable String path,
                                          @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(path, account);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

}
