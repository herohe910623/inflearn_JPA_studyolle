package com.studyolle.study;

import com.studyolle.WithAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class StudySettingsControllerTest extends StudyControllerTest {

    @Test
    @WithAccount("herohe")
    @DisplayName("스터디 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account manager = createAccount("manager");
        Study study = createStudy("test-study", manager);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("herohe")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    void updateDescriptionForm_success() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Study study = createStudy("test-study", herohe);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("herohe")
    @DisplayName("스터디 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Study study = createStudy("test-study", herohe);

        String settingsDescriptionURL = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionURL)
                        .param("shortDescription", "this is Short Description")
                        .param("fullDescription", "this is Full Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionURL))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("herohe")
    @DisplayName("스터디 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Study study = createStudy("fail-study", herohe);

        String settingsDescriptionURL = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionURL)
                        .param("shortDescription", "")
                        .param("fullDescription", "this is Full Description")
                        .with(csrf()))
                .andExpect(model().hasErrors())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

    }

}