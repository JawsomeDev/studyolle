package com.studyolle.modules.account;


import com.studyolle.modules.account.form.SignUpForm;
import com.studyolle.modules.account.validator.SignUpFormValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signupForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        log.info("권한 : " + SecurityContextHolder.getContext().getAuthentication());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signupSubmit(@Valid @ModelAttribute("signUpForm") SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()) {
            log.info("권한 : " + SecurityContextHolder.getContext().getAuthentication());
            return "account/sign-up";
        }
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if(account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return view;
        }
        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        log.info("권한 : " + SecurityContextHolder.getContext().getAuthentication());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute( account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model){
        if(!account.canSendConfirmEmail()){
            model.addAttribute(account);
            model.addAttribute("error", "인증 이메일은 1시간에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        model.addAttribute(account);
        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }


    @GetMapping("/login")
    public String loginP(){
        return "account/login";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account){
        Account accountToView = accountService.getAccount(nickname);

        model.addAttribute(accountToView);
        model.addAttribute("isOwner", accountToView.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes){
        Account account = accountRepository.findByEmail(email);
        if(account == null){
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if(!account.canSendConfirmEmail()){
            model.addAttribute("error", "로그인 이메일은 1시간에 한 번만 전송할 수 있습니다.");

            return "account/email-login";
        }
        account.generateEmailCheckToken();
        accountRepository.save(account);

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if(account == null || !account.isValidToken(token)){
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }
        accountService.login(account);
        return view;
    }
}
