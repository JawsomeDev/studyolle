package com.studyolle.modules.notification;

import com.studyolle.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepositoy extends JpaRepository<Notification, Long> {

    long countByAccountAndChecked(Account account, boolean checked);
}
