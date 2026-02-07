package com.reliaquest.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiApplicationTest {

    @Test
    void main_startsApplication() {
        ApiApplication.main(new String[]{});
    }
}
