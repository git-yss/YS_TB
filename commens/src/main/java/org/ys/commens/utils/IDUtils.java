package org.ys.commens.utils;

import java.util.Random;

/**
 * 各种id生成策略
 * @version 1.0
 */
public class IDUtils {

	/**
	 * 商品id生成
	 */
	public static long genOrderId() {
		//取当前时间的长整形值包含毫秒
		long millis = System.currentTimeMillis();
		//加上两位随机数
		Random random = new Random();
		int end2 = random.nextInt(99);
		//如果不足两位前面补0
		String str = millis + String.format("%02d", end2);
		long id = Long.parseLong(str);
		return id;
	}

}
