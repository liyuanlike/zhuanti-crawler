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
package com.github.service;

import com.alibaba.fastjson.JSONObject;
import com.github.event.TaskProcessEvent;
import com.github.model.Feedback;
import com.github.util.RegexUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


@Service
public class XimalayaService {

    @Resource private RestTemplate restTemplate;
    @Resource private ZhuantiService zhuantiService;
    @Resource private ApplicationContext applicationContext;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Map<String, Object> parse(String url) {

	    String responseContent = restTemplate.getForObject(url, String.class);
	    String title = RegexUtil.getGroup1MatchContent("itemprop=\"name\">(.+?)</h2>", responseContent) + "【有声版】";
	    String coverUrl = RegexUtil.getGroup1MatchContent("\"imgUrl\":\"(.+?)\"", responseContent);


	    List<Map<String, String>> taskList = new ArrayList<>();

	    Matcher matcher = RegexUtil.matcher("<li class=[\\s\\S]+?</li>", responseContent);
	    while (matcher.find()) {

		    String liContent = matcher.group();
		    String name = RegexUtil.getGroup1MatchContent("itemprop=\"name\">(.+?)</h4>", liContent);
		    String soundUrl = RegexUtil.getGroup1MatchContent("sound_url='(.+?)'", liContent);

		    Map<String, String> task = new HashMap<>();
		    task.put("name", name);
		    task.put("soundUrl", soundUrl);
		    taskList.add(task);
	    }


	    String albumId = url.substring(url.lastIndexOf("/") + 1);
	    int page = 2;
	    String pageUrl = "http://m.ximalaya.com/album/more_tracks?url=%2Falbum%2Fmore_tracks&aid={albumId}&page={page}";
	    do {

		    JSONObject pageJSONObject = restTemplate.getForObject(pageUrl, JSONObject.class, albumId, page);
		    page = pageJSONObject.getInteger("next_page");

		    String html = pageJSONObject.getString("html");

		    matcher = RegexUtil.matcher("<li class=[\\s\\S]+?</li>", html);
		    while (matcher.find()) {

			    String liContent = matcher.group();
			    String name = RegexUtil.getGroup1MatchContent("<h4.+?>(.+?)</h4>", liContent);
			    String soundUrl = RegexUtil.getGroup1MatchContent("sound_url='(.+?)'", liContent);

			    Map<String, String> task = new HashMap<>();
			    task.put("name", name);
			    task.put("soundUrl", soundUrl);
			    taskList.add(task);
		    }
	    } while (page > 0);

	    Map<String, Object> album = new HashMap<String, Object>(){{
			put("title", title);
			put("coverUrl", coverUrl);
			put("taskList", taskList);
	    }};

	    Feedback feedback = new Feedback(url, title, coverUrl, null);
	    feedback.setStatus(1);
	    feedback.setCount(taskList.size());
	    applicationContext.publishEvent(new TaskProcessEvent(this, feedback));
	    logger.info("解析成功, 标题: {}, 任务数量: {}", title, taskList.size());

    	return album;
    }


    @Async
    public Feedback crawl(String uid, String url, HttpHeaders headers) {

	    String courseId = null;
	    Feedback feedback = new Feedback(url);

        try {

	        Map<String, Object> album = parse(url);

	        String title = (String) album.get("title");
	        String coverUrl = (String) album.get("coverUrl");
	        List<Map<String, String>> taskList = (List<Map<String, String>>) album.get("taskList");
	        feedback = new Feedback(url, title, coverUrl, null);


	        // 创建专题
	        courseId = zhuantiService.createZhuanti(uid, title, coverUrl);
	        String chapterId = zhuantiService.createChapter(courseId, "", title, "1", headers);

            for (int i = 0; i < taskList.size(); i++) {

                Map<String, String> task = taskList.get(i);
                zhuantiService.handleChapterContent(uid, courseId, chapterId, task.get("name"), task.get("soundUrl"), headers);


                if (i == taskList.size() - 1) {
                    feedback.setStatus(3);
                } else {
                    feedback.setStatus(2);
                }
                feedback.setCourseId(courseId);
                feedback.setCurrent(i + 1);
                feedback.setCurrentName(task.get("name"));
                feedback.setCount(taskList.size());
                applicationContext.publishEvent(new TaskProcessEvent(this, feedback));
            }

            feedback.setStatus(1);

        } catch (Exception e) {

        	if (StringUtils.isNotEmpty(courseId)) {
		        zhuantiService.deleteZhuanti(courseId, headers);
	        }

	        feedback.setMessage(e.getMessage());
            feedback.setStatus(0);
	        applicationContext.publishEvent(new TaskProcessEvent(this, feedback));
	        logger.error(e.getMessage(), e);
        }

        return feedback;
    }

}

