package com.studyolle.study;


import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudyController {

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model){
        return "";
    }
}
