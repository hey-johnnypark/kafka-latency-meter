package com.github.heyjohnnypark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

}