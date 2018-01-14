package com.dfs.ace.etlcomposer.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.exception.*;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockCallable;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockRunnable;
import org.jooq.util.xml.jaxb.InformationSchema;
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
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.*;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;


@RestController
@RequestMapping("/api")
public class EtlComposerRestApiController {
@Autowired
@Value("${searchUrl}")
public String searchUrl;

    @Autowired
    @Value("${searchUrlByPattern}")
    public String searchUrlByPattern;

//    @Autowired
//    private DSLContext dsl;

//    @RequestMapping(value = "/preview/", method = RequestMethod.POST,
//            consumes = MediaType.APPLICATION_JSON_VALUE)
//    public void processPreviewRequest(@RequestBody String payload){
//
//        String input = "[{'data1':'a1','data2':'b1'},{'data1':'a2','data2':'b\'2'}]";
//        String output = input.replace("\'","\"");
//        String s1 = output;
//
//        JSONObject json;
//        JSONParser parser = new JSONParser();
//        try {
//           json  = (JSONObject) parser.parse(payload);
//           JSONObject j = json;
//        }
//        catch (ParseException e){
//
//        }
//    }

    @RequestMapping(value = "/preview/", method = RequestMethod.GET) //need to change this to POST
    public void processPreviewRequest(){

        SelectField[] flds = new SelectField[5]; //we will count the number of fields in the incoming JSON

        for (int i = 0;i<5;i++){ //here we will iterate over the fields
            flds[i] = field("field-" + Integer.toString(i)); //here we will assign each field
        }

        String sql =
        DSL.select(flds)
                .from(table("myTable")) //here we are plugging in table name from incoming JSON
        .getSQL();

        System.out.println(sql);

//        String sql = DSL.select(field("field-1"), field("field-2"), field("field-3"))
//                .from(table("myTable"))
//                .join(table("AUTHOR"))
//                .on(field("BOOK.AUTHOR_ID").eq(field("AUTHOR.ID")))
//                .where(field("BOOK.PUBLISHED_IN").eq(1948))
//                .getSQL();
//
//        String s = sql;


//        Pattern p = Pattern.compile(".\"d");
//        Matcher m = p.matcher(initialOutput);
//        String found = m.replaceAll(".'d");
//        String finalOutput = initialOutput.replace("b\"","b'");
//        String s1 = finalOutput;
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
