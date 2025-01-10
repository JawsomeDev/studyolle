package com.studyolle;

import com.studyolle.account.AccountService;
import com.studyolle.account.CustomUserDetailsService;
import com.studyolle.account.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;


public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount>{


    private final AccountService accountService;


    private final CustomUserDetailsService customUserDetailsService;

    public WithAccountSecurityContextFactory(AccountService accountService, CustomUserDetailsService customUserDetailsService) {
        this.accountService = accountService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public SecurityContext createSecurityContext(WithAccount annotation) {
        String nickname = annotation.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname + "@email.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);

        UserDetails principal = customUserDetailsService.loadUserByUsername(nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
