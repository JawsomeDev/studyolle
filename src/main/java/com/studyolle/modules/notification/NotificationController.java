package com.studyolle.modules.notification;


import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository repository;
    private final NotificationService service;


    @GetMapping("/notifications")
    public String getNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = repository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute(account);
        model.addAttribute("isNew", true);
        service.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = repository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfChecked = repository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications,notifications.size(), numberOfChecked);
        model.addAttribute(account);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @PostMapping("/notifications")
    public String deleteNotifications(@CurrentUser Account account) {
        repository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications,
                                             long numberOfChecked, long numberOfNotChekced) {

        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();
        for (var notification : notifications) {
            switch (notification.getNotificationType()){
                case STUDY_CREATED :  newStudyNotifications.add(notification); break;
                case EVENT_ENROLLMENT :  eventEnrollmentNotifications.add(notification); break;
                case STUDY_UPDATED : watchingStudyNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChekced);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }
}
