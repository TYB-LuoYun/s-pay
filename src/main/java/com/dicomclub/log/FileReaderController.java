package com.dicomclub.log;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dicomclub.common.utils.FileReaderUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author ftm
 * @date 2022/11/10 0010 10:36
 * 限流
 */
@RestController
@RequestMapping("/file-reader")
public class FileReaderController {

    @Value("${logging.file.path:''}")
    private String rootPath;
    @RequestMapping("/tail")
    public List<String> tail(String dir,String targetFile,Integer nums){
        if(StringUtils.isBlank(dir)){
            dir=rootPath;
        }
        File file = new File(dir, targetFile);
        List content= FileReaderUtil.readLastLine(file, StandardCharsets.UTF_8,2);
        return content;
    }


    @RequestMapping("/seek")
    public JSONObject seek(String dir, String targetFile, Long seek, Integer nums){
        if(StringUtils.isBlank(dir)){
            dir=rootPath;
        }
        File file = new File(dir, targetFile);
        RandomAccessFile accessFile =null;
        try {
            accessFile = new RandomAccessFile(file,"r");
            List<String> strings = FileReaderUtil.readSeekLines(accessFile, seek, nums);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", strings);
            jsonObject.put("length", accessFile.length());
            jsonObject.put("index", accessFile.getFilePointer());
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(accessFile!=null){
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    @RequestMapping("/find")
    public List<String> find(String dir, String targetFile,String find,  String mode,Integer nums,Integer times){
        if(StringUtils.isBlank(dir)){
            dir=rootPath;
        }
        File file = new File(dir, targetFile);
        List<String> content= FileReaderUtil.find(file,find,mode,nums,times);
        return content;
    }
}
