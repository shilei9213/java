package x.java.util;

import java.time.LocalDate;
import java.util.Optional;

/**
 * java.util.Optional
 * 
 * 对应容器，用于优雅的处理null
 * 
 * 特别注意：如果不是null的场景，不要使用
 * 
 */
public class OptionalDemo {

    public static void common() {
        String value = null;

        // 1 非null，操作 if(value!=null) doOption();
        Optional<String> valueOptional = Optional.ofNullable(value);
        valueOptional.ifPresent(v -> {
            // doOption();
        });

        // 2 null 检测，guava 替代
        valueOptional.orElseThrow(() -> new IllegalArgumentException("value can not be null"));

        // 3 null 时返回默认值
        value = valueOptional.orElse("other");

        value = valueOptional.orElseGet(() -> {
            String newValue = null;
            // doOption(newValue);
            return newValue;
        });

        // 功能等价
        // orElse() 遵循递归栈getDefaultValue() 一定会执行
        value = valueOptional.orElse(getDefaultValue());
        // orElseGet() 函数方法回调，getDefaultValue() 根据条件执行
        value = valueOptional.orElseGet(OptionalDemo::getDefaultValue);
    }

    private static String getDefaultValue() {
        return "";
    }

    /**
     * 过滤，主要用来多级null和其他判断条件合并的场景，有验证框架，不适合使用
     */
    public static void filter() {

        class User {
            int id;
            int age;
        }

        User user = null;
        // filter 不符合条件，返回 emptyOptional,
        // 等价：user!=null && user.id>0 && user.age>0 && user.age<200
        boolean isValidUser = Optional.ofNullable(user).filter(u -> u.id > 0).filter(u -> u.age > 0 && u.age < 200)
                .isPresent();
        System.out.print(isValidUser);
    }

    /**
     * 转换value类型 map：转换后，自动将mapper函数返回值包装成 Optional类型，并验证是否为空</br>
     * flatMap：转换后，返回mapper return的类型</br>
     */
    public static void map() {
        LocalDate today = LocalDate.now();

        String dateStr = Optional.ofNullable(today).map(n -> n.toString()).get();
        System.out.println("data : " + dateStr);

        dateStr = Optional.ofNullable(today).flatMap(n -> Optional.ofNullable(n.toString())).get();
        System.out.println("data : " + dateStr);
    }

    /**
     * 链式用法
     */
    public static void chain() {
        class City {
            Province province;

            class Province {
                Country country;
            }

            class Country {
                String name;
            }
        }

        City city = null;
        String countryName = Optional.ofNullable(city).map(c -> c.province).map(p -> p.country)
                .map(country -> country.name).orElse("unkonwn");
        System.out.println("countryName : " + countryName);
    }
}