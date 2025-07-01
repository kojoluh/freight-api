package com.fkluh.freight;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.fkluh.freight.v1.config.TestJwtDecoderConfig;

@SpringBootTest
@Import(TestJwtDecoderConfig.class)
class FreightCargoApplicationTests {

    @Test
    void contextLoads() {
    }

}
