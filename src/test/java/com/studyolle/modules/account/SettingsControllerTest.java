package com.studyolle.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.infra.AbstractContainerBaseTest;
import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.account.form.TagForm;

import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.tag.TagRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@MockMvcTest
class SettingsControllerTest extends AbstractContainerBaseTest {

    @MockitoBean
    JavaMailSender javaMailSender;
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;




    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }


    @WithAccount("keesun")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception{
        String bio = "짧은 소개를 수정하는 경우123131313131322222222222221111133333333333332312313131313142145141115151515113333333333.";
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }


    @WithAccount("keesun")
    @DisplayName("프로필 수정 하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception{
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post("/settings/profile")

                .param("bio", "짧은 소개를 수정하는 경우.")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attribute("message", "프로필을 수정했습니다."));
        Account keesun = accountRepository.findByNickname("keesun");
        Assertions.assertThat(keesun.getBio()).isEqualTo(bio);

    }
    @WithAccount("keesun")
    @DisplayName("프로필 수정 하기 - 입력값 오류")
    @Test
    void updateProfile_error() throws Exception{
        String bio = "짧은 소개를 수정하는 경우123131313131322222222222221111133333333333332312313131313142145141115151515113333333333.";
        mockMvc.perform(post("/settings/profile")
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account keesun = accountRepository.findByNickname("keesun");
        assertNull(keesun.getBio());

    }

    @WithAccount("keesun")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception{
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("keesun")
    @DisplayName("패스워드 수정 -입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception{
        mockMvc.perform(post("/settings/password")
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("settings/password"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertTrue(passwordEncoder.matches("12345678", keesun.getPassword()));
    }

    @WithAccount("keesun")
    @DisplayName("패스워드 수정 -입력값 정상")
    @Test
    void updatePassword_success() throws Exception{
        mockMvc.perform(post("/settings/password")
                .param("newPassword", "11111111")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertTrue(passwordEncoder.matches("11111111", keesun.getPassword()));
    }


    @WithAccount("keesun")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception{
        mockMvc.perform(get("/settings/tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("keesun")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("keesun").getTags().contains(newTag));
    }
    @WithAccount("keesun")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception{
        Account keesun = accountRepository.findByNickname("keesun");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(keesun, newTag);

        assertTrue(keesun.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(keesun.getTags().contains(newTag));
    }
}