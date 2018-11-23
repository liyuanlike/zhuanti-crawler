/*
 * ............................................. 
 * 
 * 				    _ooOoo_ 
 * 		  	       o8888888o 
 * 	  	  	       88" . "88 
 *                 (| -_- |) 
 *                  O\ = /O 
 *              ____/`---*\____ 
 *               . * \\| |// `. 
 *             / \\||| : |||// \ 
 *           / _||||| -:- |||||- \ 
 *             | | \\\ - /// | | 
 *            | \_| **\---/** | | 
 *           \  .-\__ `-` ___/-. / 
 *            ___`. .* /--.--\ `. . __ 
 *        ."" *< `.___\_<|>_/___.* >*"". 
 *      | | : `- \`.;`\ _ /`;.`/ - ` : | | 
 *         \ \ `-. \_ __\ /__ _/ .-` / / 
 *======`-.____`-.___\_____/___.-`____.-*====== 
 * 
 * ............................................. 
 *              佛祖保佑 永无BUG 
 *
 * 佛曰: 
 * 写字楼里写字间，写字间里程序员； 
 * 程序人员写程序，又拿程序换酒钱。 
 * 酒醒只在网上坐，酒醉还来网下眠； 
 * 酒醉酒醒日复日，网上网下年复年。 
 * 但愿老死电脑间，不愿鞠躬老板前； 
 * 奔驰宝马贵者趣，公交自行程序员。 
 * 别人笑我忒疯癫，我笑自己命太贱； 
 * 不见满街漂亮妹，哪个归得程序员？
 *
 * 北纬30.√  154518484@qq.com
 */
package com.github.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.service.ZhuantiService;
import com.github.util.RegexUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Controller
@RequestMapping
public class IndexController {

	@Resource private RestTemplate restTemplate;
	@Resource private ZhuantiService zhuantiService;

	@RequestMapping({"", "/", "index"})
	public String index(@CookieValue(required = false, value = "UID") String uid, Model model) {

		if (StringUtils.isNotEmpty(uid)) {
			JSONObject user = restTemplate.getForObject("http://passport.basicedu.chaoxing.com/user/{uid}/info", JSONObject.class, uid);
			model.addAttribute("user", user);
		}

		return "index";
	}

	@RequestMapping("task")
	public String task(HttpServletRequest request) {

		// 保存当前用户的cookie
		List<String> cookieList = new ArrayList<>();
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			cookieList.add(cookie.getName() + "=" + cookie.getValue());
		}
		HttpHeaders headers = new HttpHeaders();
		headers.put(HttpHeaders.SET_COOKIE, cookieList);
		request.getSession().setAttribute("headers", headers);

		return "task";
	}

	@RequestMapping("task/add")
	public String taskAdd(String url) {

		return "task";
	}


	@RequestMapping("logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return UrlBasedViewResolver.REDIRECT_URL_PREFIX + "http://passport2.chaoxing.com/login?refer=http://i.chaoxing.com";
	}


	@ResponseBody
	@RequestMapping("t")
	public Object t(HttpServletRequest request, HttpServletResponse response) throws IOException {

//		System.err.println(zhuantiService.yunpan("http://www.baidu.com/img/bd_logo1.png"));
//		zhuantiService.login("yaokangming@chaoxing.com", "154518484", request, response);


		return "success";
	}

}

