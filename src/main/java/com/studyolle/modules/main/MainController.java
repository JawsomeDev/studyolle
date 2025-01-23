package com.studyolle.modules.main;


import com.studyolle.modules.account.CurrentUser;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.study.Study;
import com.studyolle.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if(account!=null){
            model.addAttribute("account", account);
        }

        return "index";
    }

    @GetMapping("/search/study")
    public String searchStudy(@CurrentUser Account account, String keyword, Model model){
        List<Study> studyList = studyRepository.findByKeyword(keyword);
        if(studyList == null){
            studyList = new ArrayList<>();
        }
        model.addAttribute(account);
        model.addAttribute(studyList);
        model.addAttribute("keyword", keyword);
        return "search";
    }
}