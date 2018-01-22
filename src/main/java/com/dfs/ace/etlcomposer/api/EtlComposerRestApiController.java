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
import static org.jooq.impl.DSL.*;

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

            Object object = new Object();
            JSONParser jsonParser = new JSONParser();

            try {
                object = jsonParser.parse(payload.toString());
            }
            catch (ParseException e){

            }

            JSONObject incomingJson = (JSONObject) object;

            JSONArray fields = (JSONArray) incomingJson.get("fields");

            String tableName = incomingJson.get("tableName").toString();

            int numberOfFields = fields.size();

        SelectField[] flds = new SelectField[numberOfFields]; //we will count the number of fields in the incoming JSON

        for (int i = 0;i<numberOfFields;i++){ //here we will iterate over the fields
            JSONObject fld = (JSONObject) fields.get(i);
            String fieldName = fld.get("fieldName").toString();
            flds[i] = field(fieldName); //here we will assign each field
        }

            List<Condition> filterList = new ArrayList<>();
            filterList.add(condition("column1=Y"));
            filterList.add(condition("column2=Z"));

//            List<Condition> filterList = new ArrayList<>();
//            filterList.add(field("column1", String.class).eq(y));
//            filterList.add(field("column2", Integer.class).eq(z));

            Collection<Condition> conditions = new ArrayList<Condition>();

            conditions.addAll(filterList);

            Object obj = conditions;

        String sql =
        DSL.select(flds)
                .from(table(tableName))
                .where(conditions)
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

    }

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
