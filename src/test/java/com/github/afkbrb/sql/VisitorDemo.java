package com.github.afkbrb.sql;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 这不算测试。
 */
public class VisitorDemo {

    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String s = bufferedReader.readLine();
        System.out.print(s);
    }

    private static class Visitor {

        void visit(Father father) {
            System.out.println("father");
        }

        void visit(Son1 son1) {
            System.out.println("son1");
        }

        void visit(Son2 son2) {
            System.out.println("son2");
        }
    }

    private static class Father {

        void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    private static class Son1 extends Father {
        // 不覆盖的会会匹配父类对应的 visit 方法
    }

    private static class Son2 extends Father {
        @Override
        void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    @Test
    public void test() {
        Visitor visitor = new Visitor();
        Father father = new Father();
        father.accept(visitor); // father
        Son1 son1 = new Son1();
        son1.accept(visitor); // father
        Son2 son2 = new Son2();
        son2.accept(visitor); // son2
    }
}
