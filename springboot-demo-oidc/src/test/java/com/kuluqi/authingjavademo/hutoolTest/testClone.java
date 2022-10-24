package com.kuluqi.authingjavademo.hutoolTest;

import cn.hutool.core.clone.CloneRuntimeException;
import cn.hutool.core.clone.CloneSupport;
import cn.hutool.core.clone.Cloneable;
import org.junit.jupiter.api.Test;

public class testClone {
    public static class Dog extends CloneSupport<Dog> {
        private String name = "wangwang";
        private int age = 3;
    }

    private static class Cat implements Cloneable<Cat>{
        private String name = "miaomiao";
        private int age = 2;

        @Override
        public Cat clone() {
            try {
                return (Cat) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new CloneRuntimeException(e);
            }
        }
    }

    @Test
    public void testCat(){
        Cat cat = new Cat();
        Cat cloneCat = cat.clone();
        System.out.println(cloneCat.name);
    }
    @Test
    public void testDog(){
        Dog dog = new Dog();
        Dog clone = dog.clone();
        System.out.println(clone.name);
    }
}
