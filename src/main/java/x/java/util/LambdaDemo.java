package x.java.util;

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