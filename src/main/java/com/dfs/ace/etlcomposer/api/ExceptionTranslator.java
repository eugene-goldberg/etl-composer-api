package com.dfs.ace.etlcomposer.api;


import org.springframework.context.annotation.Bean;

public class ExceptionTranslator {
    @Bean
    public ExceptionTranslator exceptionTransformer() {
        return new ExceptionTranslator();
    }
}
