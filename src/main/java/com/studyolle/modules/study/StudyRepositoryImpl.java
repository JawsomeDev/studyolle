package com.studyolle.modules.study;

import com.querydsl.jpa.JPQLQuery;
import com.studyolle.modules.account.QAccount;
import com.studyolle.modules.tag.QTag;
import com.studyolle.modules.zone.QZone;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
public class StudyRepositoryImpl extends QuerydslRepositorySupport implements StudyRepositoryCustom{

    public StudyRepositoryImpl() {
        super(Study.class);
    }

    @Override
    public List<Study> findByKeyword(String keyword) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .distinct();
        return query.fetch();
    }
}
