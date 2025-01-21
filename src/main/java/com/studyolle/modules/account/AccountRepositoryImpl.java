package com.studyolle.modules.account;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyolle.modules.tag.QTag;
import com.studyolle.modules.zone.QZone;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public AccountRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Account> findAccountsByTagsAndZones(Set<String> tags, Set<String> zones) {
        QAccount account = QAccount.account;
        QTag tag = QTag.tag;
        QZone zone = QZone.zone;

        return queryFactory
                .selectDistinct(account) // 중복된 Account 제거
                .from(account)
                .leftJoin(account.tags, tag) // Account와 Tag의 관계 조인
                .leftJoin(account.zones, zone) // Account와 Zone의 관계 조인
                .where(
                        tag.title.in(tags) // 태그 이름이 입력받은 tagNames에 포함되어야 함
                                .or(zone.localNameOfCity.in(zones)) // 또는 지역 이름이 zoneNames에 포함되어야 함
                )
                .fetch(); // 결과를 List로 반환
    }
}
