package com.studyolle.modules.account;

import com.studyolle.infra.MockMvcTest;
import com.studyolle.infra.mail.EmailMessage;
import com.studyolle.infra.mail.EmailService;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("회원 가입 화면이 보이는지 테스트")
    public void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated())
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
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
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
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("gijin"));

        Account account = accountRepository.findByEmail("email@email.com");
        assertNotEquals(account.getPassword(), "12345678"); // 인코딩 되었기 때문에 해싱 되어서 입력한 비밀번호와 다르다.
        assertNotNull(account);
        assertTrue(accountRepository.existsByEmail("email@email.com"));
        assertNotNull(account.getEmailCheckToken());
        //SimpleMailMessage 타입의 인스턴스를 아무거나 가지고 send가 호출되었는가를 확인
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    public void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "asdfsdfsd")
                        .param("email", "email@email.com")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated())
        ;
    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 정상")
    public void checkEmailToken_correct_input() throws Exception {
        Account account = Account.builder()
                .email("test@email.com")
                .password("12345678")
                .nickname("checkEmailToken")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("checkEmailToken"));
    }
}