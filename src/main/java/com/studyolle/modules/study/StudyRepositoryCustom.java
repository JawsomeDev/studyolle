package com.studyolle.modules.study;

import java.util.List;

public interface StudyRepositoryCustom {

    List<Study> findByKeyword(String keyword);
}
