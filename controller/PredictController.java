package com.example.controller;

import com.example.common.Result;
import com.example.entity.CharTable;
import com.example.service.CharTableFormerService;
import com.example.service.CharTableService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
public class PredictController {
    @Resource
    private CharTableFormerService charTableFormerService;

    @Resource
    private CharTableService charTableService;
    
    // 文件上传存储路径
    private static final String filePath = "E:/000/小程序/小程序/小程序前后端代码/a_lat - 副本/scripts/diffusion/test_data_dir";
    
//    // conda 路径
//    // TODO
/*    private Path condaPath = Paths.get("");*/
//    // conda 环境名称
//    // TODO
//    private String envName = "";
    
    
    @PostMapping("/predict")
    public Result predict(String base64Image) {
        
        try {
            // 假设 Base64 编码的字符串是 JSON 格式：{"image":"...base64 data..."}
            String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
/*
            String pythonScriptPath = "/path/to/shibie.py";
            String imagePath = "/path/to/image.png";
            String modelPath = "/path/to/mbnet.pkl";

            String[] command = {
                    "/bin/bash", "-c",
                    "source " + "conda" +" "+"activate" +" " + pytorch + " && python " + pythonScriptPath + " " + imagePath + " " + modelPath
            };

            */
            // 指定保存图片的路径
            //获取当前时间戳用作文件名
            Long timeStamp = System.currentTimeMillis();
// fuwuqi         Path destinationFile = Paths.get("/root/jgw/upload_imgs", timeStamp + ".png");
            Path destinationFile = Paths.get("E://000//小程序//小程序//小程序前后端代码//a_lat - 副本//scripts//diffusion//upload", timeStamp + ".png");
            Files.write(destinationFile, decodedBytes);
            
            //TODO
//            Path scriptPath = Paths.get("C:\\Users\\Eazed\\oracle-bone\\script\\shibie.py");
//            Path modelPath = Paths.get("C:\\Users\\Eazed\\oracle-bone\\script\\mobilenetv3_small_best.pth");
//
//            String[] args = new String[]{"cmd.exe", "/c", condaPath.toString(), "activate", envName, "&&", "python", scriptPath.toString(), destinationFile.toString(), modelPath.toString()};


            //服务器端
//            String scriptPath = "/root/jgw/scripts_former/shibie.py";
//            String modelPath = "/root/jgw/scripts_former/mbnet.pkl";
//            String [] args = new String[]{"/root/miniconda3/envs/jgw_py38/bin/python",scriptPath,destinationFile.toString(),modelPath};
            //本地端E:\000\小程序\小程序\小程序前后端代码\a旧\script
            String scriptPath = "E:/000/小程序/小程序/小程序前后端代码/a旧/script/shibie.py";
            String modelPath = "E:/000/小程序/小程序/小程序前后端代码/a旧/script/mbnet.pkl";
            String [] args = new String[]{"cmd.exe", "/c", "conda", "activate", "yolo_py38", "&&", "python",scriptPath,destinationFile.toString(),modelPath};

            int ID;
            ID = -1;
            try {
                Process process = Runtime.getRuntime().exec(args);
                int f = process.waitFor();

                if (f == 0) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String str = in.readLine();
                    if (str != null) {
                        ID = Integer.parseInt(str);
                        System.out.println("ID:" + ID);
                    }
                } else {
                    System.out.println("error1");
                    return Result.error();
                }
            } catch (Exception e) {
                System.out.println("error2");
                return Result.error();
            }
            System.out.println("ID:" + ID);
            // 先通过 id 查询 charTableFormer ，然后根据找到的字查询 charTable
            CharTable c1 = charTableFormerService.selectByID(ID);
            CharTable c2 = charTableService.selectByChar(c1.getsWord());
            return Result.success(c2);
        } catch (Exception e) {
            System.out.println("error3");
            return Result.error();
        }
    }
    
    @PostMapping("/generate")
    public Result generate(String base64Image) throws IOException {
        String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        //获取当前时间戳用作文件名
        Long timeStamp = System.currentTimeMillis();
        Path UploadImagePath = Paths.get(filePath, timeStamp + ".jpg");
        Files.write(UploadImagePath, decodedBytes);
        
        // 入口脚本路径
        // TODO
        Path scriptPath = Paths.get("E:/000/小程序/小程序/小程序前后端代码/a_lat - 副本/scripts/diffusion/main.py");
        
        //参考图片路径
        // TODO
        Path referenceImagePath = Paths.get("E:/000/小程序/小程序/小程序前后端代码/a_lat - 副本/scripts/diffusion/example_kaishu.png");
        // String[] args = new String[]{"cmd.exe", "/c", condaPath.toString(), "activate", envName, "&&", "python", scriptPath.toString(), UploadImagePath.toString(), referenceImage.toString()};
        String [] args = new String[]{"cmd.exe", "/c", "conda", "activate", "yolo_py312", "&&", "python",scriptPath.toString(), UploadImagePath.toString(), referenceImagePath.toString()};


        try {
            Process process = Runtime.getRuntime().exec(args);

            int exitCode = process.waitFor();
            Files.deleteIfExists(UploadImagePath);
            if (exitCode != 0) {
                return Result.error();
            }

            // 读取脚本的输出
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            // 提取最后一行（假设最后一行是路径）
            String[] lines = output.toString().split(System.lineSeparator());
            if (lines.length == 0) {
                return Result.error();
            }
            String lastLine = lines[lines.length - 1].trim();

            // 检查路径是否存在
            File file = new File(lastLine);
            if (!file.exists()) {
                return Result.error();
            }

            // 读取文件内容并转换为 Base64
            byte[] bytes = Files.readAllBytes(file.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return Result.success(base64);
        } catch (Exception e) {
            System.out.println("error2");
            return Result.error();
        }
    }
}
