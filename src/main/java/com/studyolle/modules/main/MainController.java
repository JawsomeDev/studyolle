package com.studyolle.modules.main;


import com.studyolle.modules.account.CurrentUser;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.notification.NotificationRepositoy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final NotificationRepositoy notificationRepositoy;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if(account!=null){
            model.addAttribute("account", account);
        }

        long count = notificationRepositoy.countByAccountAndChecked(account, false);
        model.addAttribute("hasNotification", count > 0);
        return "index";
    }
}