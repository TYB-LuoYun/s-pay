package com.dicomclub.payment.common.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author ftm
 * @date 2022/11/10 0010 10:54
 */
public class FileReaderUtil {
    /**
     * 读取日志最后N行
     * @param s
     * @param numLastLineToRead
     * @return
     */
    public static List<String> readLastLine(File file, Charset s, int numLastLineToRead) {
        List<String> result = new ArrayList<>();
        ReversedLinesFileReader reader = null;
        try  {
            reader = new ReversedLinesFileReader(file,1024,s.name());
            String line = "";
            while ((line = reader.readLine()) != null && result.size() < numLastLineToRead) {
                result.add(line);
            }
            //倒叙遍历
            Collections.reverse(result);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    public static List<String> readSeekLines(RandomAccessFile accessFile,long seek,int lines) throws IOException {
        accessFile.seek(seek);
        String line=null;
        int current = 0;
        List<String> strings = new ArrayList<>();
        while((line=accessFile.readLine())!=null&&current<lines)
        {
            strings.add(new String(line.getBytes("ISO-8859-1"), "utf-8"));
            current++;
        }
        return strings;
    }

    public static String readSeekLine(RandomAccessFile accessFile,long seek) throws IOException {
        List<String> strings = readSeekLines(accessFile, seek, 1);
        if(CollectionUtils.isEmpty(strings)){
            return "";
        }
        return strings.get(0);
    }


    public static void main(String[] args) throws  Exception {
        String dir ="C:\\Users\\Administrator\\.qkyx\\logs\\qkyx-api\\2022-11-29";
        String targetFile = "debug-log-0.log";
        File file = new File(dir, targetFile);
        RandomAccessFile accessFile = new RandomAccessFile(file,"r");
        List<String> content= FileReaderUtil.find(file,"Caused by: java.net.ConnectException: Connection timed out: connect","after",10,2);
        if(content!=null){
            content.forEach(item->{
                System.out.println(item);
            });
        }
//      根据时间使用二分法查找，最终想要获得


    }


    public static List<String> find(File file, String find, String mode, Integer lines, Integer times) {
        if(times == null ){
            times = 1;
        }
        if(lines == null){
            lines = 100;
        }
        if(StringUtils.isEmpty(find)){
            return null;
        }
        RandomAccessFile accessFile =null;
        try {
            accessFile = new RandomAccessFile(file,"r");
            Queue<String> queue = new LinkedList<>();
            String line=null;
            int current = 0;
            boolean isContinue = true;
            int currentTime = 0;
            while((line=accessFile.readLine())!=null&&isContinue)
            {
                String read = new String(line.getBytes("ISO-8859-1"), "utf-8");
                queue.offer(read);
                if(read.contains(find)&&currentTime<times){
                    currentTime=currentTime+1;
                    if(currentTime == times){
                        if("before".equals(mode)){
//                         读完当前不用继续读了
                            isContinue = false;
                        }else if("after".equals(mode)){
//                       还需要重新读 lines行
                            current = 0;
                        }else{
//                         还需要读lines/2行
                            current = 0;
                            lines = lines/2;
                        }
                    }
                }
                current++;


                if(currentTime == times)
                {
//                  找到了
                    if("before".equals(mode)){
//                         读完当前不用继续读了
                        isContinue = false;
                    }else {
                        if(current>=lines){
                            isContinue = false;
                        }
                    }
                }else{
//                  没有找到

                }



                if( queue.size()>lines){
                    queue.poll();
                }


                System.out.println("行："+current);
            }

            if(currentTime == times){
                System.out.println("找到了");
                return (List<String>) queue;
            }else{
                System.out.println("没有找到了");
                return null;
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if(accessFile!=null){
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
