package com.sesac.aibackend.util;


import org.springframework.stereotype.Component;

@Component
public class MessageFormatterNew {

    public String format(String name) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>> edited by @Component");
        return "[INFO] Hello, " + name + "!";
    }

}
