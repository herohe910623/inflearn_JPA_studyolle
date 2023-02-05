package com.studyolle.account;

import com.studyolle.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    @DisplayName("회원 가입 화면이 보이는지 테스트")
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
        ;
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 비정상")
    public void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "gijin")
                        .param("email", "email..")
                        .param("password", "12345")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"));
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상")
    public void signUpSubmit_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "gijin")
                        .param("email", "email@email.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("email@email.com");
        assertNotEquals(account.getPassword(),"12345678"); // 인코딩 되었기 때문에 해싱 되어서 입력한 비밀번호와 다르다.
        assertNotNull(account);
        assertTrue(accountRepository.existsByEmail("email@email.com"));
        //SimpleMailMessage 타입의 인스턴스를 아무거나 가지고 send가 호출되었는가를 확인
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}