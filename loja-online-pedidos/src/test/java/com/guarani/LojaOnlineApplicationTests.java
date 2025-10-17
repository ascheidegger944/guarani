package com.guarani;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class LojaOnlineApplicationTests {

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }

    @Test
    void mainMethodStartsApplication() {
        LojaOnlineApplication.main(new String[] {});
    }
}