package com.studyolle.modules.account;


import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname + "@email.com");
        accountRepository.save(account);
        return account;
    }
}
