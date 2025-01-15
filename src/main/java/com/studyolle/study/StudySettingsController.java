package com.studyolle.study;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.CurrentUser;
import com.studyolle.account.form.TagForm;
import com.studyolle.account.form.ZoneForm;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.study.form.StudyDescriptionForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/{path}/settings")
public class StudySettingsController {

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account, @PathVariable String path, Model model, ModelMap modelMap){
        Study study = studyService.getStudyToUpdate(path, account);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }


    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(path, account);
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/description";
    }


    @GetMapping("/banner")
    public String studyImageForm(@CurrentUser Account account,@PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(path, account);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String studyImageSubmit(@CurrentUser Account account, @PathVariable String path, String image
    , RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdate(path, account);
        studyService.updateStudyImage(study, image);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
     }

     @PostMapping("/banner/enable")
     public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path){
         Study study = studyService.getStudyToUpdate(path, account);
         studyService.enableStudyBanner(study);

         return "redirect:/study/" + getPath(path) + "/settings/banner";
     }

     @PostMapping("/banner/disable")
     public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path){
         Study study = studyService.getStudyToUpdate(path, account);
         studyService.disableStudyBanner(study);

         return "redirect:/study/" + getPath(path) + "/settings/banner";
     }


    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdateTag(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitles = tagRepository.findAll().stream()
                .map(Tag::getTitle).toList();
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity<?> addTag(@CurrentUser Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTag(account, path);
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tagRepository.save(Tag.builder().title(title).build());
        }

        studyService.addTag(study, tag);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("tagTitle", title);

        return ResponseEntity.ok(response); // JSON 응답 반환
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity<?> removeTag(@CurrentUser Account account,@PathVariable String path,
                                       @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        // 계정에 태그 추가
        studyService.removeTag(study, tag);

        // 클라이언트로 응답 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", "remove");
        response.put("tagTitle", title);

        return ResponseEntity.ok(response); // JSON 응답 반환
    }

    @GetMapping("/zones")
    public String updateZonesForm(@CurrentUser Account account, @PathVariable String path,
                                  Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdateZone(account, path);
        model.addAttribute("account", account);
        model.addAttribute(study);
        model.addAttribute("zones", study.getZones().stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity<?> addZone(@CurrentUser Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm){
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
//        if(zone == null){
//            return ResponseEntity.badRequest().build();
//        }
        studyService.addZone(study, zone);
        Map<String, String> response = new HashMap<>();
        response.put("status", "add");
        response.put("cityName", zoneForm.getCityName());
        response.put("provinceName", zoneForm.getProvinceName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity<?> removeZone(@CurrentUser Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm){
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone ==null){
            System.out.println("에러에러");
            return ResponseEntity.badRequest().build();
        }
        studyService.removeZone(study, zone);
        Map<String, String> response = new HashMap<>();
        response.put("status", "remove");
        response.put("cityName", zoneForm.getCityName());
        response.put("provinceName", zoneForm.getProvinceName());
        return ResponseEntity.ok(response);
    }
}