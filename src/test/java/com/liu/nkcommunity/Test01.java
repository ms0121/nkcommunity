package com.liu.nkcommunity;

public class Test01 {
    public static void main(String[] args) {
        int a = 10;
        new Test01().f1(a);
    }
    private void f1(int a) {
        int b = 10;
        Person p = new Person();
        p.id = 111;
        p.name = "zhangsan";
        System.out.println(a + b);
    }
}
class Person {
    int id;
    String name;
}