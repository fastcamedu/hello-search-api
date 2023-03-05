package com.fastcampus.hellosearchapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
class ElasticsearchClientConfigTest {

    @MockBean
    ProductRepository productRepository;

    @Autowired
    ElasticsearchClientConfig elasticsearchClientConfig;

    @Test
    void clientConfiguration() {
        assertTrue(elasticsearchClientConfig.connetionUrl.contains("localhost"));
    }
}