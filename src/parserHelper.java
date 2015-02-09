/**
 * Created by shouhancheng on 2/8/15.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.*;

class parserHelper {
    private static final String configFilePath = "./resource/applicationConfig.json";
    private static final String rawAPIFilePath = "./uploadHolder/rawApiConf.txt";
    private static String url="";
    private static String contentType="";
    private static String id="";
    private static String auth="";
    private static String accept="";
    private static String elements="";
    private static JSONObject configs;


    private static void getConfig(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(configFilePath)));
            String content = "";
            String line = "";
            while((line = br.readLine()) != null){
                content += line;
            }
            configs = new JSONObject(content);
            url = configs.get("api.base.url").toString();
            contentType = configs.get("api.content.type").toString();
            id = configs.get("api.account.id").toString();
            auth = configs.get("api.ava.auth").toString();
            accept= configs.get("api.ava.accept").toString();
            elements = configs.get("elements").toString();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void parseRawFile(){
        getConfig();
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(rawAPIFilePath)));
            String line = "";
            String APIName = "";
            String method = "";
            String content = "";
            while((line = br.readLine()) != null){
                if(line.contains("###")){
                    APIName = line.substring(line.lastIndexOf('#') + 1, line.lastIndexOf(' '));
                    method = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                }
                if(!APIName.isEmpty() && line.contains("{")){
                    while((line = br.readLine())!= null){
                        if(line.isEmpty()){
                            break;
                        }
                        if(line.contains(":")) {
                            content += (line.substring(line.indexOf("\"") + 1, line.indexOf(":") - 1) + "\n").replaceAll("\"","");
                        }
                    }
                    writeSingleFile(APIName,method,content);
                    content = "";
                    APIName = "";
                    method = "";
                }
            }
            br.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void writeSingleFile(String APIName, String method, String content){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(APIName + ".json")));
            String finalContent = "";
            finalContent += "{\n";
            finalContent += "\t\"basePath\":" + "\"" + url + "\",\n";
            finalContent += "\t\"header\":{\n" + "\t\t\"Content-Type:\":" + "\"" + contentType + "\",\n"
                                             + "\t\t\"Authorization:\":" + "\"" + auth + "\",\n"
                                             + "\t\t\"Accept:\":" + "\"" + accept  + "\",\n"
                                             + "\t\t\"User-Agent\": \"Chrome\",\n"
                                             + "\t\t\"X-Avalara-AccountId\": \"" + id  + "\",\n"
                                             + "\t},\n"
                                             + "\t\"name\":" + "\"" + APIName + "\",\n"
                                             + "\t\"privatePath\": \"\",\n"
                                             + "\t\"protocol\": \"rest\",\n"
                                             + "\t\"httpMethod\": \"" + method + "\",\n"
                                             + "\t\"publicPath\": \"\",\n"
                                             + "\t\"body\": {\n";
            List<String> attributes = Arrays.asList(content.split("\n"));
            for(String attribute:attributes){
                finalContent += sectionBuilder(attribute);
            }
            bw.write(finalContent);
            bw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private static String sectionBuilder(String attribute){
        String open = "";
        String title = "";
        String required = "";
        String def = "";
        String description = "";
        String section = "";
        String close = "";
        try {
            JSONObject attributes = new JSONObject(elements);
            if(attributes.has(attribute)){
                JSONObject attr = new JSONObject(attributes.get(attribute).toString());
                open = "\t\t\t\"" + attribute + "\": {\n";
                title = "\t\t\t\t\"title\":" + "\"" + attribute + "\"," + "\n";
                required = "\t\t\t\t\"required\":" + "\"" + attr.get("required").toString() + "\"," + "\n";
                def = "\t\t\t\t\"default\":" + "\"" + attr.get("default").toString() + "\"," + "\n";
                description = "\t\t\t\t\"description\":" + "\"" + attr.get("description").toString() + "\"" + ",\n";
                close = "\t\t\t}\n";
                section = open + title + required + def + description + close;
            }
            else{
                section = "\t\t\"" + attribute + ":\"{\n";
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return section;
    }
}
