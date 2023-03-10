package com.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ZoneRepository zoneRepository;

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }
    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ???")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? - ????????? ??????")
    @Test
    void updateProfile() throws Exception {
        String bio = "?????? ????????? ?????? ??????";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account herohe = accountRepository.findByNickname("herohe");
        assertEquals(bio,herohe.getBio());
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? - ????????? ??????")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "?????? ?????? ?????? ????????? ?????? ????????? ???????????? ?????? 35??? ????????? ??? ????????? ???????????? ????????? ???????????? ?????? ??????. " +
                "????????? ???????????? ??????. ????????? ???????????? ????????? ??? ??????. ????????? ?????? ??? ?????? ?????? ~ 35??? ?????????????";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account herohe = accountRepository.findByNickname("herohe");
        assertNull(herohe.getBio());
    }

    @WithAccount("herohe")
    @DisplayName("???????????? ?????? ???")
    @Test
    void updatePassword_Form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("herohe")
    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                        .param("newPassword","123456789")
                        .param("newPasswordConfirm","123456789")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account herohe = accountRepository.findByNickname("herohe");
        assertTrue(passwordEncoder.matches("123456789",herohe.getPassword()));
    }

    @WithAccount("herohe")
    @DisplayName("???????????? ?????? - ????????? ?????? - ???????????? ?????????")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPasswordConfirm","12345678")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ???")
    @Test
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? - ????????? ??????")
    @Test
    void updateNickname_success() throws Exception {
        String newNickname = "herohe2";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname",newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("herohe2"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? - ????????? ?????????")
    @Test
    void updateNickname_fail() throws Exception {
        String newNickname = "\\__(?????????)__//";
        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
                .param("nickname",newNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ?????? ???")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ??????")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)    // Parameter ?????? ????????? ???????????? ????????????. Json ???????????? ????????????.
                .content(objectMapper.writeValueAsString(tagForm)) // content("\"tagTitle\": \"newTag\"}") ??????????????? ???????????? ??????.
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("herohe").getTags().contains(newTag));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ??????")
    @Test
    void removeTag() throws Exception {
        // given
        Account herohe = accountRepository.findByNickname("herohe");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(herohe,newTag);

        assertTrue(herohe.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        // when
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)    // Parameter ?????? ????????? ???????????? ????????????. Json ???????????? ????????????.
                        .content(objectMapper.writeValueAsString(tagForm)) // content("\"tagTitle\": \"newTag\"}") ??????????????? ???????????? ??????.
                        .with(csrf()))
                .andExpect(status().isOk());

        // then
        assertFalse(herohe.getTags().contains(newTag));
    }

    private Zone testZone = Zone.builder().city("test").localNameOfCity("????????????").province("????????????").build();

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ?????? ?????? ???")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_ZONES_URL))
                .andExpect(view().name(SettingsController.SETTINGS_ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ?????? ??????")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account herohe = accountRepository.findByNickname("herohe");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(herohe.getZones().contains(zone));
    }

    @WithAccount("herohe")
    @DisplayName("????????? ?????? ?????? ??????")
    @Test
    void removeZone() throws Exception{
        Account herohe = accountRepository.findByNickname("herohe");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(herohe,zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingsController.SETTINGS_ZONES_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(herohe.getZones().contains(zone));
    }

}