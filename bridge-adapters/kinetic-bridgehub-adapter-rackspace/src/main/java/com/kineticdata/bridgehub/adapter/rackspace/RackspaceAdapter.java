package com.kineticdata.bridgehub.adapter.rackspace;

import com.kineticdata.bridgehub.adapter.BridgeAdapter;
import com.kineticdata.bridgehub.adapter.BridgeError;
import com.kineticdata.bridgehub.adapter.BridgeRequest;
import com.kineticdata.bridgehub.adapter.BridgeUtils;
import com.kineticdata.bridgehub.adapter.Count;
import com.kineticdata.bridgehub.adapter.Record;
import com.kineticdata.bridgehub.adapter.RecordList;
import com.kineticdata.commons.v1.config.ConfigurableProperty;
import com.kineticdata.commons.v1.config.ConfigurablePropertyMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.LoggerFactory;

public class RackspaceAdapter implements BridgeAdapter {
    /*----------------------------------------------------------------------------------------------
     * PROPERTIES
     *--------------------------------------------------------------------------------------------*/

    /** Defines the adapter display name */
    public static final String NAME = "Rackspace Bridge";

    /** Defines the logger */
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(RackspaceAdapter.class);

    /** Adapter version constant. */
    public static String VERSION;
    /** Load the properties version from the version.properties file. */
    static {
        try {
            java.util.Properties properties = new java.util.Properties();
            properties.load(RackspaceAdapter.class.getResourceAsStream("/"+RackspaceAdapter.class.getName()+".version"));
            VERSION = properties.getProperty("version");
        } catch (IOException e) {
            logger.warn("Unable to load "+RackspaceAdapter.class.getName()+" version properties.", e);
            VERSION = "Unknown";
        }
    }

    /** Defines the collection of property names for the adapter */
    public static class Properties {
        public static final String PROPERTY_USERNAME = "Username";
        public static final String PROPERTY_PASSWORD = "Password";
        public static final String PROPERTY_REGIONS = "Regions";
    }

    private final ConfigurablePropertyMap properties = new ConfigurablePropertyMap(
        new ConfigurableProperty(Properties.PROPERTY_USERNAME).setIsRequired(true),
        new ConfigurableProperty(Properties.PROPERTY_PASSWORD).setIsRequired(true).setIsSensitive(true),
        new ConfigurableProperty(Properties.PROPERTY_REGIONS).setIsRequired(true)
    );

    private String regions;

    /**
     * The AuthInfo object that will be used to authorize the calls
     * throughout the use of the Bridge
     */
    private AuthInfo authInfo;

    /**
     * Structures that are valid to use in the bridge
     */
    public static final List<String> VALID_STRUCTURES = Arrays.asList(new String[] {
        "servers","images","flavors"
    });

    /*---------------------------------------------------------------------------------------------
     * SETUP METHODS
     *-------------------------------------------------------------------------------------------*/

    @Override
    public void initialize() throws BridgeError {
        this.regions = properties.getValue(Properties.PROPERTY_REGIONS);
//        testAuth();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void setProperties(Map<String,String> parameters) {
        properties.setValues(parameters);
    }

    @Override
    public ConfigurablePropertyMap getProperties() {
        return properties;
    }

    /*---------------------------------------------------------------------------------------------
     * IMPLEMENTATION METHODS
     *-------------------------------------------------------------------------------------------*/


    @Override
    public Count count(BridgeRequest request) throws BridgeError {
        if (!VALID_STRUCTURES.contains(request.getStructure())) {
            throw new BridgeError("Invalid Structure: " + request.getStructure() + " is not a valid structure");
        }

        // URIs to perform GETs on
        String[] regionList = this.regions.split(",");

        // Parsing the query to include parameters if they have been used.
        RackspaceQualificationParser parser = new RackspaceQualificationParser();
        String query = parser.parse(request.getQuery(),request.getParameters());

        ExecutorService pool = Executors.newFixedThreadPool(regionList.length);
        Set<Future<String>> set = new HashSet<Future<String>>();
        for (String region : regionList) {
            Callable<String> callable = new GetServersCallable(region,this.authInfo,request.getStructure(),encodeQuery(query));
            Future<String> future = pool.submit(callable);
            set.add(future);
        }

        Integer count = 0;
        for (Future<String> future : set) {
            try {
                JSONObject jsonObj = (JSONObject)JSONValue.parse(future.get());
                JSONArray servers = (JSONArray)jsonObj.get(request.getStructure());
                count = count + servers.size();
            } catch (Exception e) {
                throw new BridgeError(e);
            }
        }

        //Return the response
        return new Count(Long.valueOf(count));
    }

    @Override
    public Record retrieve(BridgeRequest request) throws BridgeError {
        List<String> fields = request.getFields();

        if (!VALID_STRUCTURES.contains(request.getStructure())) {
            throw new BridgeError("Invalid Structure: " + request.getStructure() + " is not a valid structure");
        }

        // URIs to perform GETs on
        String[] regionList = this.regions.split(",");

        // Parsing the query to include parameters if they have been used.
        RackspaceQualificationParser parser = new RackspaceQualificationParser();
        String query = parser.parse(request.getQuery(),request.getParameters());

        ExecutorService pool = Executors.newFixedThreadPool(regionList.length);
        Set<Future<String>> set = new HashSet<Future<String>>();
        for (String region : regionList) {
            Callable<String> callable = new GetServersCallable(region,this.authInfo,request.getStructure(),encodeQuery(query));
            Future<String> future = pool.submit(callable);
            set.add(future);
        }

        Record record = new Record(null);

        for (Future<String> future : set) {
            try {
                JSONObject jsonObj = (JSONObject)JSONValue.parse(future.get());
                JSONArray servers = (JSONArray)jsonObj.get(request.getStructure());
                // If server size is equal to 0, ignore. Else, move onto other possibilitites.
                if (!servers.isEmpty()) {
                    if (servers.size() > 1) {
                        throw new BridgeError("Multiple results matched an expected single match query");
                    }
                    else if (servers.size() ==1 && record != null) {
                        throw new BridgeError("Multiple results matched an expected single match query");
                    }
                    else {
                        JSONObject recordJson = (JSONObject)JSONValue.parse(servers.get(0).toString());
                        Map<String,Object> recordMap = new LinkedHashMap<String,Object>();
                        if (fields == null) { fields = new ArrayList( recordJson.entrySet()); }
                        for (String field : fields) {
                            recordMap.put(field, recordJson.get(field));
                        }
                        record = new Record(recordMap);
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            } catch (ExecutionException e) {
                logger.error(e.getMessage());
            }
        }

        // Returning the response
        return record;

    }

    @Override
    public RecordList search(BridgeRequest request) throws BridgeError {
        List<String> fields = request.getFields();

        if (!VALID_STRUCTURES.contains(request.getStructure())) {
            throw new BridgeError("Invalid Structure: " + request.getStructure() + " is not a valid structure");
        }

        // URIs to perform GETs on
        String[] regionList = this.regions.split(",");

        // Parsing the query to include parameters if they have been used.
        RackspaceQualificationParser parser = new RackspaceQualificationParser();
        String query = parser.parse(request.getQuery(),request.getParameters());

        ExecutorService pool = Executors.newFixedThreadPool(regionList.length);
        Set<Future<String>> set = new HashSet<Future<String>>();
        for (String region : regionList) {
            Callable<String> callable = new GetServersCallable(region,this.authInfo,request.getStructure(),encodeQuery(query));
            Future<String> future = pool.submit(callable);
            set.add(future);
        }

        ArrayList<Record> records = new ArrayList<Record>();

        for (Future<String> future : set) {
            try {
                JSONObject jsonObj = (JSONObject)JSONValue.parse(future.get());
                JSONArray servers = (JSONArray)jsonObj.get(request.getStructure());
                for (int i = 0; i < servers.size(); i++) {
                    JSONObject serversObj = (JSONObject)JSONValue.parse(servers.get(i).toString());
                    List record = new ArrayList();
                    for (String field : fields) {
                        if (serversObj.containsKey(field)) {
                            record.add(serversObj.get(field));
                        } else {
                            throw new BridgeError("Bad Field: A field with the name '" + field + "' could not be found");
                        }
                    }
                    records.add((Record) record);
                }
            } catch (InterruptedException e) {
                throw new BridgeError(e);
            } catch (ExecutionException e) {
                throw new BridgeError(e);
            }
        }

        // Building the output metadata
        Map<String,String> metadata = BridgeUtils.normalizePaginationMetadata(request.getMetadata());
        metadata.put("pageSize", String.valueOf("0"));
        metadata.put("pageNumber", String.valueOf("1"));
        metadata.put("offset", String.valueOf("0"));
        metadata.put("size", String.valueOf(records.size()));
        metadata.put("count", metadata.get("size"));

        // Returning the response
        return new RecordList(fields, records, metadata);
    }

    /*----------------------------------------------------------------------------------------------
     * PRIVATE HELPER METHODS
     *--------------------------------------------------------------------------------------------*/

    protected String encodeQuery(String query) {
        StringBuilder encodedQuery = new StringBuilder();
        if (!query.equals("*")) {
            String[] splitQuery = query.split("&");
            for (String parameter : splitQuery) {
                String[] splitParam = parameter.split("=");
                if (!encodedQuery.toString().equals("")) {
                    encodedQuery = encodedQuery.append("&");
                }
                encodedQuery.append(URLEncoder.encode(splitParam[0]));
                encodedQuery.append("=");
                encodedQuery.append(URLEncoder.encode(splitParam[1]));
            }
        }
        return encodedQuery.toString();
    }

    /**
     * A helper method used to parse the list of returned vm's based on the
     * inputted query.
     *
     * @param request
     * @param soapTag
     * @return
     * @throws BridgeError
     */
    protected List<List> sortResults(BridgeRequest request, JSONArray searchResults) throws BridgeError {
        final ArrayList<String> fields = new ArrayList<String>(request.getFields());
        final String order = request.getMetadata("order");
        Collections.sort(searchResults, new Comparator<JSONObject>() {

            @Override
            public int compare(JSONObject a, JSONObject b) {
                CompareToBuilder comparator = new CompareToBuilder();
                if (fields != null) {
                    for (int i=0; i < fields.size(); i++) {
                        String field = fields.get(i);
                        String valA = (String) a.get(field);
                        String valB = (String) b.get(field);
                        if (order != null) {
                            String fieldOrder = String.format("%s DESC", field);
                            if (order.contains(fieldOrder)){
                                comparator.append(valB,valA);
                            }
                        }
                    }
                }

                return comparator.toComparison();
            }
        });
        return searchResults;
    }

}

class GetServersCallable implements Callable<String> {
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(RackspaceAdapter.class);
    private String region;
    private AuthInfo authInfo;
    private String query;
    private String structure;

    protected GetServersCallable(String region, AuthInfo authInfo, String structure, String query) {
        this.region = region;
        this.authInfo = authInfo;
        this.structure = structure;
        if (!query.equals("")) {
            this.query = "?" + query;
        } else {
            this.query = "";
        }
    }

    @Override
    public String call() {
        HttpClient client = HttpClients.createDefault();
        HttpContext localContext = new BasicHttpContext();
        String url = String.format("https://%s.servers.api.rackspacecloud.com/v2/%s/%s/detail%s",this.region,this.authInfo.getAccountNumber(),this.structure,this.query);
        System.out.println(url);
        HttpGet get = new HttpGet(url);
        get.setHeader("X-Auth-Token",this.authInfo.getToken());
        get.setHeader("Content-Type","application/json");

        String jsonStr = "";
        try {
            HttpResponse response = client.execute(get,localContext);
            jsonStr = EntityUtils.toString(response.getEntity());
            logger.debug(jsonStr);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return jsonStr;
    }
}

class AuthInfo {
    private String token;
    private String account;
    private String expires;
    private String username;
    private String password;
    private String authResponse;
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(RackspaceAdapter.class);

    protected AuthInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void authenticate() throws BridgeError {
        // Creating the JSON object that will be sent containing the authorization
        // credentials for getting the token from Rackspace
        Map<String,Object> data = new LinkedHashMap();
        Map<String,Object> auth = new LinkedHashMap();
        Map<String,String> creds = new LinkedHashMap();
        creds.put("username",this.username);
        creds.put("password",this.password);
        auth.put("passwordCredentials", creds);
        data.put("auth", auth);
        String jsonData = JSONValue.toJSONString(data);

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://identity.api.rackspacecloud.com/v2.0/tokens");
        HttpResponse response;

        try {
            // Setting up and executing the SOAP request
            StringEntity postBody = new StringEntity(jsonData);
            post.setEntity(postBody);
            post.setHeader("Content-Type","application/json");
            response = client.execute(post);
        } catch (UnsupportedEncodingException e) {
            throw new BridgeError(e);
        } catch (IOException e) {
            throw new BridgeError(e);
        }

        try {
            this.authResponse = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new BridgeError("Error converting the Rackspace response to a string",e);
        }

        if (response.getStatusLine().getStatusCode() == 401) {
            throw new BridgeError("Unable to authenticate user with credentials provided");
        } else if (response.getStatusLine().getStatusCode() != 200) {
            throw new BridgeError("Connection Error: Encountered error code " + Integer.toString(response.getStatusLine().getStatusCode()) + ". See log for details.");
        }

        // Parsing through the nested JSON structure to get to the 'token'
        // and 'expired' values
        JSONObject jsonObj = (JSONObject)JSONValue.parse(this.authResponse);
        JSONObject accessObj = (JSONObject)JSONValue.parse(JSONValue.toJSONString(jsonObj.get("access")));
        JSONObject tokenObj = (JSONObject)JSONValue.parse(JSONValue.toJSONString(accessObj.get("token")));

        this.token = tokenObj.get("id").toString();
        this.expires = tokenObj.get("expires").toString();

        JSONObject accountObj = (JSONObject)JSONValue.parse(JSONValue.toJSONString(tokenObj.get("tenant")));
        this.account = accountObj.get("id").toString();
    }

    public String getToken() {
        return this.token;
    }

    public String getAccountNumber() {
        return this.account;
    }

    // Check if the token should be expired,
    public Boolean checkForExpiredToken() throws BridgeError{
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date expireDate;
        try {
            expireDate = (Date)formatter.parse(this.expires);
        } catch (java.text.ParseException e) {
            throw new BridgeError(e);
        }

        Date currentDate = new Date();
        Boolean change = false;
        if (currentDate.after(expireDate)) {
            authenticate();
            change = true;
        }
        return change;
    }

    public void checkRegions(String regions) throws BridgeError {
        // Parsing through the nested JSON structure to get all the possible
        // region values.
        ArrayList<String> regionList = new ArrayList<String>();
        JSONObject jsonObj = (JSONObject)JSONValue.parse(this.authResponse);
        JSONObject accessObj = (JSONObject)JSONValue.parse(JSONValue.toJSONString(jsonObj.get("access")));
        JSONArray serviceCatalogObj = (JSONArray)JSONValue.parse(JSONValue.toJSONString(accessObj.get("serviceCatalog")));
        for (int i = 0; i < serviceCatalogObj.size(); i++) {
            JSONObject catalogArrayObj = (JSONObject)JSONValue.parse(JSONValue.toJSONString(serviceCatalogObj.get(i)));
            if (catalogArrayObj.get("name").toString().equals("cloudServersOpenStack")) {
                JSONArray endpointsArrayObj = (JSONArray)JSONValue.parse(JSONValue.toJSONString(catalogArrayObj.get("endpoints")));
                for (int j = 0; j < endpointsArrayObj.size(); j++) {
                    JSONObject endpointObj = (JSONObject)JSONValue.parse(JSONValue.toJSONString(endpointsArrayObj.get(j)));
                    regionList.add(endpointObj.get("region").toString().toLowerCase());
                }
                break;
            }
        }
        String[] regionSplit = regions.split(",");
        for (String region : regionSplit) {
            if (!regionList.contains(region)) {
                throw new BridgeError(String.format("Bad Region: '%s' is not a valid server for the Rackspace Cloud Servers",region));
            }
        }
    }
}