package com.example.renamefile.controller;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
public class IndexController {

    @RequestMapping("/rename")
    public List<String> rename(@RequestParam String dirPath) {
//        String dirPath = "E:\\宝宝照片\\202301";
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        List list = new ArrayList();
        for (File file : files) {
            try {
                if (file.getName().equals(".DS_Store")) {
                    continue;
                }
                Metadata metadata = null;
                try {
                    metadata = ImageMetadataReader.readMetadata(file);
                } catch (ImageProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String name = file.getName();
                String suffix = name.substring(name.lastIndexOf(".")).toUpperCase();
                Date date = null;
                switch (suffix) {
                    case ".MOV":
                        QuickTimeDirectory quickTimeDirectory = metadata.getFirstDirectoryOfType(QuickTimeVideoDirectory.class);
                        date = quickTimeDirectory.getDate(QuickTimeVideoDirectory.TAG_CREATION_TIME);
                        break;
                    case ".JPG":
                    case ".JPEG":
                    case ".PNG":
                        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                        if (directory == null) {
                            continue;
                        }
                        date = directory.getDateOriginal();
                        break;
                }
                if (date != null) {
                    String format = DateFormatUtils.format(date, "yyyy-MM-dd hhmmss").concat(suffix);
                    boolean b = file.renameTo(new File(dirPath.concat("\\").concat(format)));
                    String info = String.format("重命名：%s ==> %s %s", name, format, b);
                    if (!b) {
                        format = format.concat("01");
                        b = file.renameTo(new File(dirPath.concat("\\").concat(format)));
                        info = String.format("重命名1：%s ==> %s %s", name, format, b);
                    }
                    list.add(info);
                }
            } catch (Exception e) {
                continue;
            }

        }
        return list;
    }
}