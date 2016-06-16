/**
 * Project Name:packageweb
 * File Name:HomeController
 * Package Name:cn.cnic.bigdatalab.packageweb.controller
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
import java.util.Arrays;
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
            @RequestParam(defaultValue = "") String target,
            @RequestParam(defaultValue = "") String softname,
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
        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("message", "No File Upload");
            return "redirect:/";
        }
        if(StringUtils.isBlank(softname)){
            redirectAttributes.addFlashAttribute("message", "name is null");
            return "redirect:/";
        }
        if(StringUtils.isBlank(target)){
            redirectAttributes.addFlashAttribute("message", "target type is null");
            return "redirect:/";
        }

        long time = System.currentTimeMillis();
        //long time = 1465955810301L;
        String path = Application.ROOT + "/" + time ;
        String targetPath = path + "/target";
        new File(path).mkdir();
        new File(targetPath).mkdir();
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
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
        if(ext.equalsIgnoreCase("rpm")){
            source = "rpm";
        }else if(ext.equalsIgnoreCase("tar")){
            source = "tar";
        }else if(ext.equalsIgnoreCase("tar.gz")){
            source = "tar";
        }else if(ext.equalsIgnoreCase("deb")){
            source = "deb";
        }else if(ext.equalsIgnoreCase("zip")){
            source = "zip";
        }else{
            redirectAttributes.addFlashAttribute("message", "Not Support file type");
            return "redirect:/";
        }
        //gen shell
        if(StringUtils.isNotBlank(before_install)){
            genShell("before_install.sh",before_install,path);
        }
        if(StringUtils.isNotBlank(after_install)){
            genShell("after_install.sh",after_install,path);
        }
        if(StringUtils.isNotBlank(before_remove)){
            genShell("before_remove.sh",before_remove,path);
        }
        if(StringUtils.isNotBlank(after_remove)){
            genShell("after_remove.sh",after_remove,path);
        }
        if(StringUtils.isNotBlank(before_upgrade)){
            genShell("before_upgrade.sh",before_upgrade,path);
        }
        if(StringUtils.isNotBlank(after_upgrade)){
            genShell("after_upgrade.sh",after_upgrade,path);
        }

        List<String> targetArrays = Arrays.asList(target.split(","));

        for (String targetTemp: targetArrays
             ) {
            StringBuffer cmd = new StringBuffer();
            cmd.append("fpm ");
            cmd.append(" -s " + source);
            cmd.append(" -t " + targetTemp);
            cmd.append(" -n " + softname);
            cmd.append(" -v " + version);
            cmd.append(" --iteration " + release);
            cmd.append(" -p " + targetPath);


            if(StringUtils.isNotBlank(before_install)){
                cmd.append(String.format(" --before-install %s/before_install.sh",path));
            }
            if(StringUtils.isNotBlank(after_install)){
                cmd.append(String.format(" --after-install %s/after_install.sh",path));
            }
            if(StringUtils.isNotBlank(before_remove)){
                cmd.append(String.format(" --before-remove %s/before_remove.sh",path));
            }
            if(StringUtils.isNotBlank(after_remove)){
                cmd.append(String.format(" --after-remove %s/after_remove.sh",path));
            }
            if(StringUtils.isNotBlank(before_upgrade)){
                cmd.append(String.format(" --before-upgrade %s/before_upgrade.sh",path));
            }
            if(StringUtils.isNotBlank(after_upgrade)){
                cmd.append(String.format(" --after-upgrade %s/after_upgrade.sh",path));
            }

            cmd.append(" " + path + "/" + file.getOriginalFilename());

            command(cmd.toString());
        }


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

    private void genShell(String fileName,String content,String path)  {
        try {
            BufferedOutputStream stream = new BufferedOutputStream(
                    new FileOutputStream(new File(path +"/" + fileName)));
            FileCopyUtils.copy(content.getBytes(), stream);
            stream.close();

            command(String.format("chmod 755 %s/%s",path,fileName));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void command(String cmd){
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";

            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            logger.info("command info :{}",output.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
