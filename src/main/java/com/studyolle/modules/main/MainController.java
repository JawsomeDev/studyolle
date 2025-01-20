package com.studyolle.modules.main;


import com.studyolle.modules.account.CurrentUser;
import com.studyolle.modules.account.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if(account!=null){
            model.addAttribute("account", account);
        }
        return "index";
    }
}