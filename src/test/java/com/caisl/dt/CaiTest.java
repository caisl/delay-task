package com.caisl.dt;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * CaiTest
 *
 * @author caisl
 * @since 2019-05-08
 */
public class CaiTest {

    public static void main(String args[]){
        System.out.println(TimeUnit.HOURS.toMillis(1));
        System.out.println(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        for(int i =0;i<=10;i++) {
            System.out.println(ThreadLocalRandom.current().nextInt(3));
        }

    }
}
