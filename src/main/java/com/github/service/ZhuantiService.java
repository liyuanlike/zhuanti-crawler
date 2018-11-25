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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.util.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;


@Service
public class ZhuantiService {

    @Resource private RestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<String> login(String userName, String password, HttpServletRequest request, HttpServletResponse response) {
        String requestUrl = "http://passport2.chaoxing.com/api/login";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("name", userName);
        body.add("pwd", password);
        body.add("ip", Utils.getRequestIp(request));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity httpEntity = new HttpEntity(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestUrl, httpEntity, String.class);

        return responseEntity.getHeaders().get("Set-Cookie");
    }

    public String createZhuanti(String userId, String name, String coverUrl) {

        String url = "http://mooc1-api.chaoxing.com/RestfulAPI/addVideoCourseByUserId";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("userId", userId);
        body.add("courseName", name);
        body.add("courseDescription", name);
        body.add("courseimgurl", coverUrl);
//        body.add("courseImage", "File");
        body.add("dtype", "ZT");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity httpEntity = new HttpEntity(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);

        JSONObject object = JSONObject.parseObject(responseEntity.getBody());
        return object.getString("courseid");
    }

    public String createChapter(String courseId, String parentId, String name, String layer, HttpHeaders headers) {

        String url = "http://mooc1.chaoxing.com/edit/createchapter";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("name", name);
        body.add("courseid", courseId);
        body.add("parentid", parentId);
        body.add("layer", layer);

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity httpEntity = new HttpEntity(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        String content = responseEntity.getBody();
        logger.info(responseEntity.getHeaders().toString());

        return JSON.parseObject(content).getString("id");
    }

    public void addContent(String courseId, String chapterId, String name, JSONObject sound, HttpHeaders headers) {

        String url = "http://mooc1.chaoxing.com/edit/savecard";

        String content = "<p><iframe frameborder=\"0\" scrolling=\"no\" class=\"ans-module ans-insertaudio-module\" module=\"insertaudio\" data=\"{&quot;objectid&quot;:&quot;%s&quot;,&quot;name&quot;:&quot;%s&quot;,&quot;size&quot;:%s,&quot;hsize&quot;:&quot;%s&quot;,&quot;type&quot;:&quot;.m4a&quot;,&quot;mid&quot;:&quot;%s&quot;,&quot;_jobid&quot;:%s,&quot;jobid&quot;:%s,&quot;retract&quot;:&quot;false&quot;,&quot;module&quot;:&quot;insertaudio&quot;}\"></iframe><br/></p>";
//        content = String.format(content, "objectId", "name", "size", "hsize", "mid", "_jobid", "jobid");
        content = String.format(content, sound.getString("objectid"), name,
                sound.getInteger("length"),
                FileUtils.byteCountToDisplaySize(sound.getInteger("length")),
                RandomStringUtils.randomNumeric(16),
                System.currentTimeMillis(),
                System.currentTimeMillis());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("description", content);
        body.add("nodeid", chapterId);
        body.add("courseid", courseId);

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity httpEntity = new HttpEntity(body, headers);
        System.err.println(restTemplate.postForObject(url, httpEntity, String.class));
    }

    public JSONObject yunpan(String uid, String url) throws IOException {

        File tempFile = File.createTempFile(FilenameUtils.getBaseName(url), "." + FilenameUtils.getExtension(url));

        RequestEntity requestEntity = RequestEntity.get(URI.create(url)).build();
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);
        byte[] downloadContent = responseEntity.getBody();
        FileUtils.writeByteArrayToFile(tempFile, downloadContent);


        String uploadUrl = "http://passport.basicedu.chaoxing.com/upload/yunpan";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("uid", uid);
        body.add("uploadFile", new FileSystemResource(tempFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        JSONObject jsonObject = restTemplate.postForObject(uploadUrl, httpEntity, JSONObject.class);
        String objectId = jsonObject.getString("objectid");

        jsonObject = restTemplate.getForObject("http://passport.basicedu.chaoxing.com/upload/yunpan/{objectId}/status", JSONObject.class, objectId);

        return jsonObject;
    }

    public void handleChapterContent(String uid, String courseId, String chapterId, String name, String soundUrl, HttpHeaders headers) throws IOException {
        // 下载
        JSONObject sound = this.yunpan(uid, soundUrl);
        // 创建章节
        String contentChapterId = this.createChapter(courseId, chapterId, name, "2", headers);
        // 保存内容章节
        this.addContent(courseId, contentChapterId, name, sound, headers);
    }

    public void deleteZhuanti(String courseId) {
        String url = "http://yz4.chaoxing.com/subject_creation/SubjectCreationController/subjectHandle?type=delSubject&course_Id={courseId}";
        restTemplate.getForObject(url, String.class, courseId);
    }

}

