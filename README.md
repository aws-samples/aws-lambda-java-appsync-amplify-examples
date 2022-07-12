# Using a Amplify generated Java Lambda function to call a AppSync API.

Before we begin, make sure you have the following installed:

[Node.js](https://nodejs.org/) v14.x or later

[npm](https://www.npmjs.com/) v6.14.4 or later

[git](https://git-scm.com/) v2.14.1 or later

This tutorial assumes that you're familiar with both JavaScript/ES6 and React. If you need to brush up on React, we recommend going through the official tutorial.

## Install Amplify CLI

`npm install -g @aws-amplify/cli`

Now it's time to setup the Amplify CLI. Configure Amplify by running the following command:

`amplify configure`

## Create a new React App

```
npx create-react-app react-amplified
cd react-amplified
```

## Initialize a new backend

`amplify init`

When you initialize Amplify you'll be prompted for some information about the app, with the option to accept recommended values:

```
Enter a name for the project (amplified)
The following configuration will be applied:

Project information
| Name: amplified
| Environment: dev
| Default editor: Visual Studio Code
| App type: javascript
| Javascript framework: react
| Source Directory Path: src
| Distribution Directory Path: build
| Build Command: npm run-script build
| Start Command: npm run-script start

? Initialize the project with the above configuration? No
Enter a name for the environment (dev)

# Sometimes the CLI will prompt you to edit a file, it will use this editor to open those files.
Choose your default editor

# Amplify supports JavaScript (Web & React Native), iOS, and Android apps
Choose the type of app that you're building (javascript)

What JavaScript framework are you using (react)

Source directory path (src)

Distribution directory path (build)

Build command (npm run-script build)

Start command (npm run-script start)

Select the authentication method you want to use: (Use arrow keys)
❯ AWS profile
  AWS access keys

# This is the profile you created with the `amplify configure` command in the introduction step.
Please choose the profile you want to use (Use arrow keys)
```

## Add a AppSync GraphQL API.

Run the following command.

`amplify add api`

```
? Select from one of the below mentioned services: GraphQL
? Here is the GraphQL API that we will create. Select a setting to edit or continue Authorization modes: API key (default, expiration time: 7 days from now)
? Choose the default authorization type for the API API key
√ Enter a description for the API key: · API key
√ After how many days from now the API key should expire (1-365): · 7
? Configure additional auth types? Yes
? Choose the additional authorization types you want to configure for the API IAM
? Here is the GraphQL API that we will create. Select a setting to edit or continue Continue
? Choose a schema template: Single object with fields (e.g., “Todo” with ID, name, description)
```

Depending on your requirement, select API key or IAM as your authorization.

We can call an AppSync GraphQL API from a Java app or a Lambda function. Let's take a basic Todo app as an example:


### API key
```
type Todo @model @auth(rules: [{ allow: public }]) {
  name: String
  description: String
}
```

### IAM Auth

```
type Todo @model @auth(rules: [{ allow: private, provider: iam }]) {
  name: String
  description: String
}
```

Paste the API key or IAM auth schema into the `schema.graphql` file.

This API will have operations available for Query, Mutation, and Subscription. Let's take a look at how to perform a mutation from a Lambda function using Java.

### Pushing the Schema to the cloud.

Please run `amplify push`

## Create a Lambda function with access to AppSync API

Run the following command:

`amplify add function`

```
? Select which capability you want to add: Lambda function (serverless function)
? Provide an AWS Lambda function name: <NAME FOR FUNCTION>
? Choose the runtime that you want to use: Java
Only one template found - using Hello World by default.

Available advanced settings:
- Resource access permissions
- Scheduled recurring invocation
- Lambda layers configuration
- Environment variables configuration
- Secret values configuration

? Do you want to configure advanced settings? Yes
? Do you want to access other resources in this project from your Lambda function? Yes
? Select the categories you want this function to have access to. api
? Select the operations you want to permit on appsyncjava Query, Mutation, Subscription

You can access the following resource attributes as environment variables from your Lambda function
        API_<APP NAME>_GRAPHQLAPIENDPOINTOUTPUT
        API_<APP NAME>_GRAPHQLAPIIDOUTPUT
        API_<APP NAME>_GRAPHQLAPIKEYOUTPUT
        ENV
        REGION
? Do you want to invoke this function on a recurring schedule? No
? Do you want to enable Lambda layers for this function? No
? Do you want to configure environment variables for this function? No
? Do you want to configure secret values this function can access? No
```

The examples on this page use Amazon HTTP Client to make a HTTP request to our GraphQL API.

## Adding dependencies to build.gradle

### API key

```
dependencies {
    compile (
        'com.amazonaws:aws-lambda-java-core:1.2.0',
        'com.amazonaws:aws-lambda-java-events:3.11.0',
        'commons-lang:commons-lang:2.3',
        'com.amazonaws:aws-java-sdk-lambda:1.11.106',
        'com.jayway.jsonpath:json-path:2.4.0',
        'com.googlecode.json-simple:json-simple:1.1.1',
        'org.apache.commons:commons-lang3:3.8.1',
        'com.google.code.gson:gson:2.8.6',
        'commons-io:commons-io:2.6',
        'com.fasterxml.jackson.core:jackson-databind:2.12.1',
        'com.fasterxml.jackson.core:jackson-core:2.12.1'
    )
}
```

### IAM Auth

```
dependencies {
    compile (
        'com.amazonaws:aws-lambda-java-core:1.2.0',
        'com.amazonaws:aws-lambda-java-events:3.11.0',
        'commons-lang:commons-lang:2.3',
        'com.amazonaws:aws-java-sdk-lambda:1.11.106',
        'com.jayway.jsonpath:json-path:2.4.0',
        'com.googlecode.json-simple:json-simple:1.1.1',
        'org.apache.commons:commons-lang3:3.8.1',
        'com.google.code.gson:gson:2.8.6',
        'commons-io:commons-io:2.6',
        'com.fasterxml.jackson.core:jackson-databind:2.12.1',
        'com.fasterxml.jackson.core:jackson-core:2.12.1',
        'com.amazonaws:aws-java-sdk-appsync:1.12.255'
    )
}

```

## Mutation

In this example we create a mutation showing how to pass in variables as arguments to create a Todo record.
### API key

```java
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

import java.util.Map;
import java.net.URI;
import java.io.ByteArrayInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;


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
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    @Override
    public String handleRequest(Map<String,String> event, Context context) {
        
        // Input for the GraphQL query
        String input = "{\"name\":\"Hello,Todo!\"}";
        
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        
        try {
			json = (JSONObject) parser.parse(input);
 
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        // Create the GraphQL query
        JSONObject inputJson = createInputs(json, "mutation MyMutation($input: CreateTodoApiKeyInput!) {createTodoApiKey(input: $input) {id name}}", "MyMutation");
		System.out.println("Input Query: "+ inputJson.toString());
		
		
		// Create the HTTP request
		Request<AmazonWebServiceRequest> request = new DefaultRequest<AmazonWebServiceRequest>("appsync");
		request.setHttpMethod(HttpMethodName.POST);
		request.setEndpoint(URI.create(System.getenv("API_<APP NAME>_GRAPHQLAPIENDPOINTOUTPUT"))); // GRAPHQLAPIENDPOINTOUTPUT from the environment variables
		
		byte[] inputBytes = inputJson.toJSONString().getBytes();
		request.setContent(new ByteArrayInputStream(inputBytes));
		request.addHeader("x-api-key", System.getenv("API_<APP NAME>_GRAPHQLAPIKEYOUTPUT")); // API Key from the environment variables
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		request.addHeader(HttpHeaders.CONTENT_LENGTH,String.valueOf(inputBytes.length));
		
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
```

### IAM Auth

```java
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
        request.setEndpoint(URI.create(System.getenv("API_<APP NAME>_GRAPHQLAPIENDPOINTOUTPUT"))); // GRAPHQLAPIENDPOINTOUTPUT from the environment variables
        
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
```

Depending on your App name created the environment variables may differ, change the `<APP NAME>` to your application name.

### Pushing the Lambda functions to the cloud

Run the command: `amplify push`
### Testing the Lambda Function

After the push has been completed, Login to your AWS console and open the Lambda functions console. Select the appropriate function name created.

Click on the test tab present beside code. Then click on test. The output should appear as follows.
![image](https://user-images.githubusercontent.com/87995712/178560264-2e2c275f-1cdb-4f9d-9ccc-7411bd012729.png)
