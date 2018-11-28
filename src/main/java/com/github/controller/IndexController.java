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
import com.github.service.XimalayaService;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


@Controller
@RequestMapping
public class IndexController {

	@Resource private RestTemplate restTemplate;
	@Resource private XimalayaService ximalayaService;


	@RequestMapping({"", "/", "index"})
	public String index(@CookieValue(required = false, value = "UID") String uid, HttpServletRequest request, Model model) {

		if (StringUtils.isNotEmpty(uid)) {
			JSONObject user = (JSONObject) request.getSession().getAttribute("user");
			if (user == null) {
				user = restTemplate.getForObject("http://passport.basicedu.chaoxing.com/user/{uid}/info", JSONObject.class, uid);
				request.getSession().setAttribute("user", user);
			}
			model.addAttribute("user", user);
		}

		return "index";
	}

	@RequestMapping("task")
	public String task() {
		return "task";
	}

	@ResponseBody
	@PostMapping("task/add")
	public String taskAdd(String urls, @CookieValue("UID") String uid, HttpServletRequest request) {

		HttpHeaders headers = getHttpHeaders(request);

		StringTokenizer stringTokenizer = new StringTokenizer(urls);
		while(stringTokenizer.hasMoreElements()){
			String url = stringTokenizer.nextToken();
			if (StringUtils.isNotEmpty(url)) {
				ximalayaService.crawl(uid, url.trim(), headers);
			}
		}
		return "success";
	}


	@RequestMapping("logout")
	public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				cookie.setValue(null);
				cookie.setDomain("chaoxing.com");
				cookie.setPath("/");
				cookie.setMaxAge(0);
				response.addCookie(cookie);
			}
		}
		session.invalidate();
		return UrlBasedViewResolver.REDIRECT_URL_PREFIX + "http://passport2.chaoxing.com/login?refer=http://crawler.basicedu.chaoxing.com/";
	}


	@ResponseBody
	@RequestMapping("t")
	public Object t(HttpServletRequest request) throws Exception {

		String url = "http://m.ximalaya.com/58389790/album/14317967";
//		ximalayaService.parse(url);

		url = "http://i.mooc.chaoxing.com/space/index";
		HttpHeaders headers = getHttpHeaders(request);
		HttpEntity httpEntity = new HttpEntity(headers);
//		System.err.println(restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class));
		System.err.println(request.getSession().getId());
		System.err.println(httpEntity);

		return "success";
	}



	public static HttpHeaders getHttpHeaders(HttpServletRequest request) {

		HttpHeaders headers =null;
		String uid = WebUtils.getCookie(request, "UID").getValue();
		if (StringUtils.isNotEmpty(uid)) {

			headers = (HttpHeaders) request.getSession().getAttribute("headers");
			if (headers == null) {
				List<String> cookieList = new ArrayList<>();
				Cookie[] cookies = request.getCookies();
				for (Cookie cookie : cookies) {
					String name = cookie.getName();
					if (name.equals("cookiecheck")) {
						continue;
					}
					cookieList.add(name + "=" + cookie.getValue());
				}
				headers = new HttpHeaders();
				headers.put(HttpHeaders.COOKIE, cookieList);
				Assert.notNull(headers, "HttpHeaders is null.");

				request.getSession().setAttribute("headers", headers);
			}
		}
		return headers;

	}

}


