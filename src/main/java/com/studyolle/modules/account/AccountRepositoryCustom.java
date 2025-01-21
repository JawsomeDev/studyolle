package com.studyolle.modules.account;

import java.util.List;
import java.util.Set;

public interface AccountRepositoryCustom {
    List<Account> findAccountsByTagsAndZones(Set<String> tags, Set<String> zones);
}
