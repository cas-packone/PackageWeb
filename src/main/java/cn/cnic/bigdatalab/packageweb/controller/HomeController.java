/**
 * Project Name:packageweb
 * File Name:HomeController
 * Package Name:com.github.bigdatalab.packageweb.controller
 * Author : moonx
 * Date:2016/6/14
 * Copyright (c) 2016,  All Rights Reserved.
 * <p>
 * <p>
 *                            _ooOoo_
 *                           o8888888o
 *                           88" . "88
 *                           (| -_- |)
 *                            O\ = /O
 *                        ____/`---'\____
 *                      .   ' \\| |// `.
 *                       / \\||| : |||// \
 *                     / _||||| -:- |||||- \
 *                       | | \\\ - /// | |
 *                     | \_| ''\---/'' | |
 *                      \ .-\__ `-` ___/-. /
 *                   ___`. .' /--.--\ `. . __
 *                ."" '< `.___\_<|>_/___.' >'"".
 *               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 *                 \ \ `-. \_ __\ /__ _/ .-` / /
 *         ======`-.____`-.___\_____/___.-`____.-'======
 *                            `=---='
 * ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * 佛祖保佑       永无BUG
 * 佛曰:
 * 写字楼里写字间，写字间里程序员；
 * 程序人员写程序，又拿程序换酒钱。
 * 酒醒只在网上坐，酒醉还来网下眠；
 * 酒醉酒醒日复日，网上网下年复年。
 * 但愿老死电脑间，不愿鞠躬老板前；
 * 奔驰宝马贵者趣，公交自行程序员。
 * 别人笑我忒疯癫，我笑自己命太贱；
 * 不见满街漂亮妹，哪个归得程序员？
 */
package cn.cnic.bigdatalab.packageweb.controller;

import cn.cnic.bigdatalab.packageweb.Application;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by moonx on 2016/6/14.
 */
@Controller
public class HomeController {

    private static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    String index() {
        return "index";
    }
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    String upload(
            @RequestParam(defaultValue = "tar") String source,
            @RequestParam(defaultValue = "rpm") String target,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1.0") String version,
            @RequestParam(defaultValue = "1") String release,
            @RequestParam(defaultValue = "") String before_install,
            @RequestParam(defaultValue = "") String after_install,
            @RequestParam(defaultValue = "") String before_remove,
            @RequestParam(defaultValue = "") String after_remove,
            @RequestParam(defaultValue = "") String before_upgrade,
            @RequestParam(defaultValue = "") String after_upgrade,
            @RequestParam("file") MultipartFile file,
                  RedirectAttributes redirectAttributes,
                  HttpServletRequest request,
            HttpSession session,
            Model model) {
        //long time = System.currentTimeMillis();
        long time = 1465955810301L;
        String path = Application.ROOT + "/" + time ;
        String targetPath = path + "/target";
        //new File(path).mkdir();
        //new File(resultPath).mkdir();
        if (!file.isEmpty()) {
            try {
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(new File(path +"/" + file.getOriginalFilename())));
                FileCopyUtils.copy(file.getInputStream(), stream);
                stream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        StringBuffer command = new StringBuffer();
        command.append("fpm ");
        command.append(" -s " + source);
        command.append(" -t " + target);
        command.append(" -n " + name);
        command.append(" -v " + version);
        command.append(" --iteration " + release);
        command.append(" " + file.getOriginalFilename());

        model.addAttribute("result",command.toString());

        File targetDir = new File(targetPath);
        File[] files = targetDir.listFiles();
        List<String> fileList = new ArrayList<>();
        for (File item: files
             ) {
            fileList.add(item.getName());
        }

        model.addAttribute("file",fileList);
        session.setAttribute("dir",targetPath);
        return "result";
    }
    @RequestMapping(value = "/download/{name}/", method = RequestMethod.GET)
    void download(@PathVariable String name, HttpSession session,
                  HttpServletResponse response) throws IOException {

        if(StringUtils.isBlank(name)){
            //throw Exception();
        }
        String dir = (String)session.getAttribute("dir");
        File file = new File(dir+"/"+name);
        if(!file.exists()){

        }
        response.setHeader("content-disposition", "attachment;filename="
                + URLEncoder.encode(name, "UTF-8"));
        FileInputStream in = new FileInputStream(dir+"/"+name);
        OutputStream out = response.getOutputStream();
        byte buffer[] = new byte[1024];
        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }
}
