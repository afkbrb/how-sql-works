package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.expressions.LikeExpression;
import com.github.afkbrb.sql.ast.expressions.StringExpression;
import com.github.afkbrb.sql.model.Row;
import com.github.afkbrb.sql.model.Schema;
import com.github.afkbrb.sql.model.TypedValue;
import com.github.afkbrb.sql.visitors.AbstractEvaluator;
import com.github.afkbrb.sql.visitors.RowEvaluator;
import org.junit.Test;

import java.util.Arrays;

/**
 * 这不算测试。
 */
public class VisitorDemo {

    @Test
    public void test() {
        System.out.println("a+".matches("a\\+"));
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

    public static void main(String[] args) {
        Visitor visitor = new Visitor();
        Father father = new Father();
        father.accept(visitor); // father
        Son1 son1 = new Son1();
        son1.accept(visitor); // father
        Son2 son2 = new Son2();
        son2.accept(visitor); // son2
    }
}
