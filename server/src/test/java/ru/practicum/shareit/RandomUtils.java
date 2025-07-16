package ru.practicum.shareit;

import java.util.Random;
import java.util.UUID;

public class RandomUtils {

    private static final String[] DOMAINS = { "gmail.com", "yahoo.com", "outlook.com", "test.com" };
    private static final Random random = new Random();

    public static String getRandomEmail() {
        String name = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        String domain = DOMAINS[random.nextInt(DOMAINS.length)];
        return name + "@" + domain;
    }
}

