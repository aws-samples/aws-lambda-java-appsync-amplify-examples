/* Amplify Params - DO NOT EDIT
	API_APPSYNCJAVA_GRAPHQLAPIENDPOINTOUTPUT
	API_APPSYNCJAVA_GRAPHQLAPIIDOUTPUT
	API_APPSYNCJAVA_GRAPHQLAPIKEYOUTPUT
	ENV
	REGION
Amplify Params - DO NOT EDIT */

package example;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.DefaultRequest;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponseHandler;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.services.appsync.model.AuthenticationType;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import java.util.Map;
import java.net.URI;
import java.io.ByteArrayInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaRequestHandler implements RequestHandler<Map<String,String>, String>{   
    
    @SuppressWarnings("unchecked")
    private JSONObject createInputs(JSONObject obj, String queryType, String operationName) {
        JSONObject inputJson = new JSONObject();
        JSONObject varL1 = new JSONObject();
        JSONObject varL2 = new JSONObject();    
        for(Object key : obj.keySet()) {
            String fieldName = (String)key;
            Object fieldValue = obj.get(key);
            varL2.put(fieldName, fieldValue);
        }    
        varL1.put("input", varL2);
        System.out.println(varL1.toString());
        inputJson.put("query", queryType);
        inputJson.put("variables",varL1);   
        inputJson.put("operationName", operationName);
        return inputJson;
    }
    
    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        // Query Inputs
        String input = "{\"name\":\"Hello,Todo!\"}";
        
        JSONParser parser = new JSONParser();

        JSONObject json = null;
        
        try {
            json = (JSONObject) parser.parse(input);          
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        
        // Create the Query
        JSONObject inputJson = createInputs(json, "mutation MyMutation($input: CreateTodoIamInput!) {createTodoIam(input: $input) {id name}}", "MyMutation");
        System.out.println("Input: " +inputJson.toString());
        
        
        
        Request<AmazonWebServiceRequest> request = new DefaultRequest<AmazonWebServiceRequest>("appsync");
        request.setHttpMethod(HttpMethodName.POST);
        request.setEndpoint(URI.create(System.getenv("API_APPSYNCJAVA_GRAPHQLAPIENDPOINTOUTPUT"))); // GRAPHQLAPIENDPOINTOUTPUT from the environment variables
        
        byte[] inputBytes = inputJson.toJSONString().getBytes();
        request.setContent(new ByteArrayInputStream(inputBytes));
        request.addHeader("type", AuthenticationType.AWS_IAM.toString());
        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.addHeader(HttpHeaders.CONTENT_LENGTH,String.valueOf(inputBytes.length));
        
        
            //Signing with V4 authentication
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("appsync"); 
        signer.setRegionName("us-east-1");
        
        DefaultAWSCredentialsProviderChain credentials = new DefaultAWSCredentialsProviderChain();
        
        signer.sign(request, credentials.getCredentials());

        
        HttpResponseHandler<String> responseHandler = new HttpResponseHandler<String>(){
            
            @Override
            public String handle(HttpResponse response) throws Exception {
                
                return IOUtils.toString(response.getContent());
            }
            
            @Override
            public boolean needsConnectionLeftOpen() {
                return false;
            }
        };
        
        HttpResponseHandler<AmazonServiceException> errorHandler = new HttpResponseHandler<AmazonServiceException>() {
        @Override
        public AmazonServiceException handle(HttpResponse response) throws Exception { 
            String res = IOUtils.toString(response.getContent());
            System.out.println("ERROR ++++++ "+res);
            return new AmazonServiceException("Error ....");                                                                  
        }

        @Override
        public boolean needsConnectionLeftOpen() {
            return true;
        }
      };

        
        Response<String> rsp = new AmazonHttpClient(new ClientConfiguration())
                .requestExecutionBuilder()
                .executionContext(new ExecutionContext(true))
                .request(request)
                .errorResponseHandler(errorHandler)
                .execute(responseHandler);
        System.out.println("Query Result: " +rsp.getAwsResponse());
        
        return rsp.getAwsResponse();
        
    
    }
}