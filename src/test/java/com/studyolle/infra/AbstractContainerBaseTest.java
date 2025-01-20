package com.studyolle.infra;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractContainerBaseTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:17.2") // 사용할 Postgres 버전
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");
    @BeforeAll
    static void setUp() {
        // 1. PostgreSQL 컨테이너 실행


        POSTGRES_CONTAINER.start();

        // 2. 컨테이너에서 생성된 정보로 환경 변수 설정
        System.setProperty("TESTCONTAINERS_POSTGRES_URL", POSTGRES_CONTAINER.getJdbcUrl());
        System.setProperty("TESTCONTAINERS_POSTGRES_USERNAME", POSTGRES_CONTAINER.getUsername());
        System.setProperty("TESTCONTAINERS_POSTGRES_PASSWORD", POSTGRES_CONTAINER.getPassword());
    }
}
