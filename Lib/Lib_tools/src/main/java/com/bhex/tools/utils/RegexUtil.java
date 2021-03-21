/*
 * *******************************************************************
 *   @项目名称: BHex Android
 *   @文件名称: RegexUtil.java
 *   @Date: 18-11-29 下午4:05
 *   @Author: ppzhao
 *   @Description:
 *   @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 *   注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的.
 *  *******************************************************************
 */

package com.bhex.tools.utils;

import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * 正则工具类
 * 提供验证邮箱、手机号、电话号码、身份证号码、数字等方法
 */
public final class RegexUtil {

	/**
	 * 验证Email
	 * @param email email地址，格式：zhangsan@sina.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkEmail(String email) {
//		String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
		String regex = "^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$";
		return Pattern.matches(regex, email);
	}
	
	/**
	 * 验证身份证号码
	 * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkIdCard(String idCard) {
		String regex = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
		return Pattern.matches(regex,idCard);
	}
	
	/**
	 * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
	 * @param mobile 移动、联通、电信运营商的号码段
	 *<p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
	 *、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
	 *<p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
	 *<p>电信的号段：133、153、180（未启用）、189</p>
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkMobile(String mobile) {
		String regex = "(\\+\\d+)?1[34578]\\d{9}$";
		return Pattern.matches(regex,mobile);
	}

	/**
	 * 匹配密码规则 密码8-20位字符，必须包含大小写字母和数字
	 * @param passwd
	 * @return
	 */
	public static boolean checkPasswd(String passwd) {
		String regex = "^(?=.*?[A-Z])(?=(.*[a-z]){1,})(?=(.*[\\d]){1,}).{8,255}$";
		return Pattern.matches(regex,passwd);
	}

	/**
	 * 匹配密码规则 密码6-20位字符^{6,20}$
	 * 匹配密码规则 密码6-20位字符，必须包含大小写字母和数字 ^[A-Za-z0-9]{6,20}$
	 * 匹配密码规则 密码仅限6-20位数字 ^[0-9]{6,20}$
	 * @param passwd
	 * @return
	 */
	public static boolean checkFundPasswd(String passwd) {
//		String regex = "^[0-9]{6,20}$";
//		return Pattern.matches(regex,passwd);
		if (TextUtils.isEmpty(passwd)) {
			return false;
		}else{
			return passwd.length()>=6 && passwd.length()<=20;
		}
	}
	
	/**
	 * 验证固定电话号码
	 * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
	 * <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
	 *  数字之后是空格分隔的国家（地区）代码。</p>
	 * <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
	 * 对不使用地区或城市代码的国家（地区），则省略该组件。</p>
	 * <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkPhone(String phone) {
		String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
		return Pattern.matches(regex, phone);
	}
	
	/**
	 * 验证整数（正整数和负整数）
	 * @param digit 一位或多位0-9之间的整数
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkDigit(String digit) {
		String regex = "\\-?[1-9]\\d+";
		return Pattern.matches(regex,digit);
	}
	
	/**
	 * 验证整数和浮点数（正负整数和正负浮点数）
	 * @param decimals 一位或多位0-9之间的浮点数，如：1.23，233.30
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkDecimals(String decimals) {
		String regex = "\\-?[1-9]\\d+(\\.\\d+)?";
		return Pattern.matches(regex,decimals);
	}


	public static boolean checkNumeric(String decimals){
		String regex = "^-?\\d+(\\.\\d+)?$";
		return Pattern.matches(regex,decimals);
	}
	
	/**
	 * 验证空白字符
	 * @param blankSpace 空白字符，包括：空格、\t、\n、\r、\f、\x0B
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkBlankSpace(String blankSpace) {
		String regex = "\\s+";
		return Pattern.matches(regex,blankSpace);
	}
	
	/**
	 * 验证中文
	 * @param chinese 中文字符
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkChinese(String chinese) {
		String regex = "^[\u4E00-\u9FA5]+$";
		return Pattern.matches(regex,chinese);
	}
	
	/**
	 * 是否是2-15位中文
	 * @param chinese
	 * @return
	 */
	public static boolean checkName(String chinese) {
		String regex = "^[\u4E00-\u9FA5]{2,15}+$";
		return Pattern.matches(regex,chinese);
	}
	
	/**
	 * 是否是16-19位字母或数字
	 * 银行卡
	 * @param
	 * @return
	 */
	public static boolean checkBankCard(String str) {
		String regex = "[0-9A-Za-z]{16,19}";
		return Pattern.matches(regex,str);
	}
	
	/**
	 * 是否是6-10位字母或数字
	 * @return
	 */
	public static boolean checkLetterAndNum(String str) {
		String regex = "[0-9A-Za-z]{6,10}";
		return Pattern.matches(regex,str);
	}

	/**
	 * 是否字母或数字
	 * @return
	 */
	public static boolean isLetterDigit(String str) {
		String regex = "^[a-z0-9A-Z]+$";
		return Pattern.matches(regex, str);
	}

	/**
	 * 是否是6位数字
	 * @return
	 */
	public static boolean checkNum(String str) {
		String regex = "[0-9]{6}";
		return Pattern.matches(regex,str);
	}
	
	/**
	 * 验证日期（年月日）
	 * @param birthday 日期，格式：1992-09-03，或1992.09.03
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkBirthday(String birthday) {
		String regex = "[1-9]{4}([-./])\\d{1,2}\\1\\d{1,2}";
		return Pattern.matches(regex,birthday);
	}
	
	/**
	 * 验证URL地址
	 * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkURL(String url) {
		String regex = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?";
		return Pattern.matches(regex, url);
	}
	
	/**
	 * 匹配中国邮政编码
	 * @param postcode 邮政编码
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkPostcode(String postcode) {
		String regex = "[1-9]\\d{5}";
		return Pattern.matches(regex, postcode);
	}
	
	/**
	 * 匹配IP地址(简单匹配，格式，如：192.168.1.1，127.0.0.1，没有匹配IP段的大小)
	 * @param ipAddress IPv4标准地址
	 * @return 验证成功返回true，验证失败返回false
	 */
	public static boolean checkIpAddress(String ipAddress) {
		String regex = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))";
		return Pattern.matches(regex, ipAddress);
	}

	/**
	 * 匹配密码是否包含数字
	 * @param pwd
	 * @return
	 */
	public static boolean checkContainNum(String pwd){
		String regex = ".*\\d+.*";
		return Pattern.matches(regex, pwd);
	}


	/**
	 * 匹配密码是否包含大写字母
	 * @param pwd
	 * @return
	 */
	public static boolean checkContainUpper(String pwd){
		String regex = ".*[A-Z]+.*";
		return Pattern.matches(regex, pwd);
	}

	/**
	 * 匹配密码是否包含小写字母
	 * @param pwd
	 * @return
	 */
	public static boolean checkContainLower(String pwd){
		String regex = ".*[a-z]+.*";
		return Pattern.matches(regex, pwd);
	}

	/**
	 * 是否包括字母和空格
	 * @param origin
	 * @return
	 */
	public static boolean checkIsLetter(String origin){
		String regex = "^[A-Za-z ]*$";
		return Pattern.matches(regex, origin);
	}

	/**
	 *   String regex="^[A-Fa-f0-9]+$";
	 * @return
	 */
	public static boolean checkIsHex(String origin){
		String regex="^[A-Fa-f0-9]+$";
		return Pattern.matches(regex, origin);
	}

	/**
	 * 是否小写字母和数字
	 * @return
	 */
	public static boolean checkLowerLetterAndNum(String str) {
		String regex = "^[a-z]+[a-z0-9]*$";
		return Pattern.matches(regex,str);
	}

	//判断密码是否合法
	public static boolean pwdIsLegal(String numOrStr){
		boolean flag = isOrderNumericPlus(numOrStr) || isOrderNumeric_(numOrStr) || equalCharacter(numOrStr);
		return flag;
	}

	//不能全是相同的数字或者字母（如：000000、111111、aaaaaa） 全部相同返回true
	public static boolean equalCharacter(String numOrStr){
		if(TextUtils.isEmpty(numOrStr)){
			return true;
		}
		boolean flag = true;
		char str = numOrStr.charAt(0);
		for (int i = 0; i < numOrStr.length(); i++) {
			if (str != numOrStr.charAt(i)) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	//不能是连续的数字--递增（如：123456、12345678）连续数字返回true
	public static boolean isOrderNumericPlus(String numOrStr){
		boolean flag = true;//如果全是连续数字返回true
		boolean isNumeric = true;//如果全是数字返回true
		for (int i = 0; i < numOrStr.length(); i++) {
			if (!Character.isDigit(numOrStr.charAt(i))) {
				isNumeric = false;
				break;
			}
		}
		if (isNumeric) {//如果全是数字则执行是否连续数字判断
			for (int i = 0; i < numOrStr.length(); i++) {
				if (i > 0) {//判断如123456
					int num = Integer.parseInt(numOrStr.charAt(i)+"");
					int num_ = Integer.parseInt(numOrStr.charAt(i-1)+"")+1;
					if (num != num_) {
						flag = false;
						break;
					}
				}
			}
		} else {
			flag = false;
		}
		return flag;
	}

	//不能是连续的数字--递减（如：987654、876543）连续数字返回true
	public static boolean isOrderNumeric_(String numOrStr){
		boolean flag = true;//如果全是连续数字返回true
		boolean isNumeric = true;//如果全是数字返回true
		for (int i = 0; i < numOrStr.length(); i++) {
			if (!Character.isDigit(numOrStr.charAt(i))) {
				isNumeric = false;
				break;
			}
		}
		if (isNumeric) {//如果全是数字则执行是否连续数字判断
			for (int i = 0; i < numOrStr.length(); i++) {
				if (i > 0) {//判断如654321
					int num = Integer.parseInt(numOrStr.charAt(i)+"");
					int num_ = Integer.parseInt(numOrStr.charAt(i-1)+"")-1;
					if (num != num_) {
						flag = false;
						break;
					}
				}
			}
		} else {
			flag = false;
		}
		return flag;
	}


}
