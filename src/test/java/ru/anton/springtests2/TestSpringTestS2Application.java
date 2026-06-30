package ru.anton.springtests2;

import org.springframework.boot.SpringApplication;

public class TestSpringTestS2Application {

    public static void main(String[] args) {
        SpringApplication.from(SpringTestS2Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
