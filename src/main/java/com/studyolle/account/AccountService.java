package com.studyolle.account;


import com.studyolle.account.form.SignUpForm;
import com.studyolle.domain.Account;
import com.studyolle.account.form.Notifications;
import com.studyolle.account.form.Profile;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.mail.EmailMessage;
import com.studyolle.mail.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;


    public Account processNewAccount(@Valid SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    public Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        EmailMessage emailMessage = EmailMessage.builder()
                .from("hyuk2000s@gmail.com")
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message("/check-email-token?token=" + newAccount.getEmailCheckToken() +
        "&email=" + newAccount.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);

        // 세션에 SecurityContext 반영
        ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest().getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext());
    }


    public void completeSignUp(Account account) {
        account.compileSignUp();
        login(account);
    }

    public void updateProfile(Account account, @Valid Profile profile ) {
        modelMapper.map(profile, account);
        accountRepository.save(account);
        //TODO 프로필 이미지
    }

    public void updatePassword(Account account, @Length(min = 8, max = 50) String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, @Valid Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        EmailMessage emailMessage = EmailMessage.builder()
                        .from("hyuk2000s@gmail.com")
                        .to(account.getEmail())
                        .subject("스터디올래, 로그인 링크")
                        .message("/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail())
                        .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent( a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> {
            a.getZones().add(zone); // Account 객체에 Zone 추가// 변경된 Account 저장
        });

    }

    public void removeZone(Account account, Zone zone){
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a-> a.getZones().remove(zone));
    }
}
