package org.whiteandpaint.rhyus.value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CustomValue {
    public static void init() {
        String fileName = "adminKey"; // 文件名
        File file = new File(fileName);

        try {
            if (!file.exists()) {
                boolean isCreated = file.createNewFile();
                if (isCreated) {
                    System.out.println("The adminKey file not exists, create new: " + file.getAbsolutePath());
                }
            }

            String content = new String(Files.readAllBytes(Paths.get(fileName)));
            Config.adminKey = content.replaceAll("\r", "").replaceAll("\n", "");
        } catch (IOException e) {
            System.err.println("Get adminKey file error：" + e.getMessage());
        }

        File sslFile = new File("sslCert");
        sslFile.mkdirs();
    }
}
