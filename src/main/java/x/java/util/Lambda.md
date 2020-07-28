# 概述

Lambda 表达式，又称闭包，函数付给变量使用（本质：简化匿名内部类的定义）

> 底层：编译成主类的是有方法, 所以可以用this来调用主类

* 函数式接口：接口中只有一个抽象方法（其他可以包含：默认方法，静态方法，Object类的public同名方法）
* @FunctionalInterface：符合函数式接口定义的，都可用用lambda表达式，@FunctionalInterface只为了标记，通知开发人员，java不强制要求携带。

注：可以集成，方法可以抛异常，可以去交集

# demo

``` java
public class LambdaDemo {

    static void demo() {
        IHelloWordService helloWordService = user -> "Hello " + user;

        System.out.println(helloWordService.to("bar"));
    }

    @FunctionalInterface
    static interface IHelloWordService {

        /**
         * lambda调用方法，可以抛异常
         */
        String to(String user) throws IllegalArgumentException;

        /**
         * 默认方法
         */
        default String defaultHelloTo(String user) {
            return "hello" + user;
        }

        /**
         * 静态方法
         */
        static String helloTo(String user) {
            return "hello" + user;
        }

        /**
         * Object的public同名方法
         */
        String toString();
    }

}

```

# 通用函数式接口

jdk（java.util.function）中预定义了不少函数式接口，方便复用和沟通。

|名称|描述|
|----|----|
| Consumer< T >       | 接收T对象，不返回值           |
| Predicate< T >      | 接收T对象并返回boolean        |
| Function< T, R >    | 接收T对象，返回R对象          |
| Supplier< T >       | 提供T对象，不接收值|     
| UnaryOperator< T >  | 接收T对象，返回T对象          |
| BiConsumer<T, U>    | 接收T对象和U对象，不返回值     |
| BiPredicate<T, U>   | 接收T对象和U对象，返回boolean  |
| BiFunction<T, U, R> | 接收T对象和U对象，返回R对象    |
| BinaryOperator< T > | 接收两个T对象，返回T对象       |

# 总结

* 惰性调用：和函数调用不通，传递表达式，不会立即求职，只有在内部合适时机才调用表达式求值（类似于callback的执行）
* 函数式编程多用于赋值场景，传递表达式，参数计算获得新的值
