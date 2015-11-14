package x.java.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {

	public static void match(String patternStr, String str) {
		Pattern p = Pattern.compile(patternStr);
		Matcher m = p.matcher(str);

		while (m.find()) {
			// 匹配到一段String,打印匹配到的字符串
			System.out.println("match str : " + m.group(0));

			// 获得该匹配字符串的组信息
			int groupCount = m.groupCount();

			for (int i = 0; i <= groupCount; i++) {
				// 按照括号分组，提取括号中中的数据，注括号?:开始的会忽略
				System.out.println(i + " : " + m.group(i));
			}
			System.out.println("---------------------------------");
		}
	}

	public static void main(String[] args) {
		String pattern = "addToCart\\.html\\?(?:.*?&?)pid=(\\d+)(?=&|$)";
		String url = "http://cart.jd.com/addToCart.html?pid=1579700&rid=1440731282075&em=http://cart.jd.com/addToCart.html?rcd=1&pid=1579700&rid=1440731282075&em=http://cart.jd.com/addToCart.html?rcd=1&pid=1579700&rid=1440731282075&em=";

		match(pattern, url);

		pattern = "(?:&|\\?)id=(\\d{1,})(?=\\&|$)";
		url = "https://detail.tmall.com/item.htm?spm=a220m.1000858.1000725.96.4ZjU0q&id=45459179544&areaId=110000&cat_id=2&rn=200bdc6f7ed229a4b8cbfb2be604007b&user_id=1917047079&is_b=1";
		match(pattern, url);
	}
}
