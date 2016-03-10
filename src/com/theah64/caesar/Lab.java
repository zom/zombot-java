package com.theah64.caesar;

import java.util.Random;

/**
 * Created by theapache64 on 11/3/16.
 */
public class Lab {

    public static void main(String[] args) {
        final Random random = new Random();
        for (int i = 0; i < 100; i++) {
            System.out.println(random.nextInt(2));
        }
    }
}
