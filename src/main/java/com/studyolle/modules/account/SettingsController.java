package com.studyolle.modules.account;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.modules.account.form.*;
import com.studyolle.modules.account.validator.NicknameValidator;
import com.studyolle.modules.account.validator.PasswordFormValidator;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import com.studyolle.modules.tag.TagRepository;
import com.studyolle.modules.zone.ZoneRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Controller
public class SettingsController {

    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final AccountService accountService;
    private final NicknameValidator nicknameValidator;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping("/settings/profile")
    public String profileUpdateFrom(@CurrentUser Account account, Model model){
        model.addAttribute("account", account);
        model.addAttribute("profile", modelMapper.map(account, Profile.class));
        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute("account", account);
            return "settings/profile";
        }
        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/settings/profile";
    }

    @GetMapping("/settings/password")
    public String updatePasswordForm(@CurrentUser Account account, Model model){
        model.addAttribute("account", account);
        model.addAttribute(new PasswordForm());
        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute("account", account);
            return "settings/password";
        }
        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String updateNotificationsForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/notifications";
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/settings/notifications";
    }

    @GetMapping("/settings/tags")
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "settings/tags";
    }

    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity<?> addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();

        // 태그 검색 또는 저장
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(title).build());
        }

        // 계정에 태그 추가
        accountService.addTag(account, tag);

        // 클라이언트로 응답 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("tagTitle", title);

        return ResponseEntity.ok(response); // JSON 응답 반환
    }
    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity<?> removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        // 계정에 태그 추가
        accountService.removeTag(account, tag);

        // 클라이언트로 응답 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", "remove");
        response.put("tagTitle", title);

        return ResponseEntity.ok(response); // JSON 응답 반환
    }



    @GetMapping("/settings/account")
    public String updateAccountForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/account";
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/settings/account";
    }

    @GetMapping("/settings/zones")
    public String updateZonesForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute("account", account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "settings/zones";
    }

    @PostMapping("/settings/zones/add")
    @ResponseBody
    public ResponseEntity<?> addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
//        if(zone == null){
//            return ResponseEntity.badRequest().build();
//        }
        accountService.addZone(account, zone);

        Map<String, String> response = new HashMap<>();
        response.put("status", "add");
        response.put("cityName", zoneForm.getCityName());
        response.put("provinceName", zoneForm.getProvinceName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/settings/zones/remove")
    @ResponseBody
    public ResponseEntity<?> removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone ==null){

            System.out.println("에러에러");
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        Map<String, String> response = new HashMap<>();
        response.put("status", "remove");
        response.put("cityName", zoneForm.getCityName());
        response.put("provinceName", zoneForm.getProvinceName());
        return ResponseEntity.ok(response);
    }

}
