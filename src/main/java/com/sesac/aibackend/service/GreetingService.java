package com.sesac.aibackend.service;

import com.sesac.aibackend.util.MessageFormatter;
import com.sesac.aibackend.util.MessageFormatterNew;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GreetingService {

//    private final MessageFormatter formatter;
//    private final MessageFormatterNew formatter;

    public String hello(String name) {
//        return formatter.format(name);
        return "[INFO] Hello, " + name + "!";
    }

}
