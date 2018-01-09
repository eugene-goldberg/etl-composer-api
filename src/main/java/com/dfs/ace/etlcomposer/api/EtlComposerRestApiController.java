package com.dfs.ace.etlcomposer.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EtlComposerRestApiController {
@Autowired
@Value("${searchUrl}")
public String searchUrl;

    @Autowired
    @Value("${searchUrlByPattern}")
    public String searchUrlByPattern;

    @RequestMapping(value = "/preview/", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void processPreviewRequest(@RequestBody String payload){
        JSONObject json;
        JSONParser parser = new JSONParser();
        try {
           json  = (JSONObject) parser.parse(payload);
           JSONObject j = json;
        }
        catch (ParseException e){

        }
    }

//    private JSONArray fetchAll() {
//        JSONParser jsonParser = new JSONParser();
//        JSONArray results = new JSONArray();
//        Object object;
//        try {
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpGet getRequest = new HttpGet(
//                    searchUrl);
//            getRequest.addHeader("accept", "application/json");
//
//            HttpResponse response = httpClient.execute(getRequest);
//
//            if (response.getStatusLine().getStatusCode() != 200) {
//                throw new RuntimeException("Failed : HTTP error code : "
//                        + response.getStatusLine().getStatusCode());
//            }
//
//            BufferedReader br = new BufferedReader(
//                    new InputStreamReader((response.getEntity().getContent())));
//
//            String output;
//            System.out.println("Output from Server .... \n");
//            while ((output = br.readLine()) != null) {
//
//                try {
//                    object = jsonParser.parse(output);
//                    JSONObject jsonObject = (JSONObject) object;
//                    JSONObject outerHits =  (JSONObject) jsonObject.get("hits");
//                    JSONArray innerHits = (JSONArray) outerHits.get("hits");
//
//                    if(innerHits!=null && innerHits.size()>0) {
//                        for (int i = 0; i < innerHits.size(); i++) {
//                            JSONObject _outerSource = (JSONObject) innerHits.get(i);
//                            JSONObject _innerSource = (JSONObject) _outerSource.get("_source");
//                            results.add(_innerSource);
//                            System.out.println(_innerSource);
//                        }
//                    }
//                }
//                catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            httpClient.getConnectionManager().shutdown();
//        }
//        catch (ClientProtocolException e) {
//
//            e.printStackTrace();
//
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//
//        return results;
//    }

    @RequestMapping(value = "/metadata/match/", method = RequestMethod.GET)
    @GetMapping
    private JSONArray fetchMatch(HttpServletRequest request) {
        String queryString="";
        try {
             queryString = java.net.URLDecoder.decode(request.getQueryString(), "UTF-8");
        }
        catch(UnsupportedEncodingException e){

        }
        String  qs = queryString.replace("\"","")
                .replace(" ","*")
                .replace("q=","");

        StringBuilder sb = new StringBuilder(qs);
        sb.insert(0,'"')
                .insert(1,'*')
                .insert(sb.length(),'*')
                .insert(sb.length(),'"');

        String s = sb.toString();

        String url = "";

        try {
            url = searchUrlByPattern + "q=" + java.net.URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e){}


        JSONParser jsonParser = new JSONParser();
        JSONArray results = new JSONArray();
        Object object;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String finalReq = url;
            System.out.println(finalReq);
            HttpGet getRequest = new HttpGet(
                    finalReq);
            getRequest.addHeader("accept", "application/json");

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {

                try {
                    object = jsonParser.parse(output);
                    JSONObject jsonObject = (JSONObject) object;
                    JSONObject outerHits =  (JSONObject) jsonObject.get("hits");
                    JSONArray innerHits = (JSONArray) outerHits.get("hits");

                    if(innerHits!=null && innerHits.size()>0) {
                        for (int i = 0; i < innerHits.size(); i++) {
                            JSONObject _outerSource = (JSONObject) innerHits.get(i);
                            JSONObject _innerSource = (JSONObject) _outerSource.get("_source");
                            results.add(_innerSource);
                            System.out.println(_innerSource);
                        }
                    }
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            httpClient.getConnectionManager().shutdown();
        }
        catch (ClientProtocolException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return results;
    }
}
