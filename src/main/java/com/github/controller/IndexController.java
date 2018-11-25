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
import com.github.model.Feedback;
import com.github.service.XimalayaService;
import com.github.service.ZhuantiService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

@Controller
@RequestMapping
public class IndexController implements InitializingBean {

	@Resource private RestTemplate restTemplate;
	@Resource private ZhuantiService zhuantiService;
	@Resource private XimalayaService ximalayaService;

	@Resource private Executor executor;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static CompletionService<Feedback> completionService = null;
	@Override
	public void afterPropertiesSet() {
		completionService = new ExecutorCompletionService<>(executor);
	}


	@RequestMapping({"", "/", "index"})
	public String index(@CookieValue(required = false, value = "UID") String uid, HttpServletRequest request, Model model) {

		if (StringUtils.isNotEmpty(uid)) {

			HttpHeaders headers = (HttpHeaders) request.getSession().getAttribute("headers");
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
				request.getSession().setAttribute("headers", headers);
			}

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
	public String taskAdd(String url, @CookieValue("UID") String uid, HttpServletRequest request) {
		HttpHeaders headers = (HttpHeaders) request.getSession().getAttribute("headers");
		ximalayaService.crawl(uid, url, headers);
		return "success";
	}


	@RequestMapping("logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return UrlBasedViewResolver.REDIRECT_URL_PREFIX + "http://passport2.chaoxing.com/login?refer=http://i.chaoxing.com";
	}


	@ResponseBody
	@RequestMapping("t")
	public Object t(HttpServletRequest request) throws IOException {

		System.err.println(request.getSession().getId());

		HttpHeaders headers = (HttpHeaders) request.getSession().getAttribute("headers");
		HttpEntity httpEntity = new HttpEntity(headers);
		String url = "http://i.mooc.chaoxing.com/space/index";
		System.err.println(restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class));

		return "success";
	}

}

