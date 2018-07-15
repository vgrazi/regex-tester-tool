package com.vgrazi.regextester;

import com.vgrazi.regextester.component.RegexTester;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class RegexTesterApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(RegexTesterApplication.class);
        builder.headless(false).run(args);
    }

    @Override
    public void run(String... args) {
        new RegexTester().launch();
    }
}
