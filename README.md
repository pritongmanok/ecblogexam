# Blog Microservice

This application is used for creating blog spaces and blog entries.  It
allows the user to self register, create blog spaces, and allow other
users to publish to their blog spaces.  For a full description of the features,
consult the Technical Requirements and Implementation document found in the 
design folder.

## Supporting Documents

For full description of the features, implementation, and deployment, refer 
to the supplemental documentation below.

1. docs/Requirements and Implemention.docx
2. docs/AWS API Gateway Configuration.docx
3. docs/Entity Relationship Diagram.jpg

## Prerequisites

Prepare the following in your development machine.
- Java 1.8
- Maven 3.x
- AWS command line interface

## Building the deployment package

1.  Download/clone this repository.
2.  Modify src/java/resources/application.properties  to point to the desired relational database.
3.  Create the schema and tables in the database by executing the DDL script located at src/main/resources/ddl-mysql.sql
4.  Build the package by running the following on the command line.
    ```
    cd ${repo_local_workspace}
    mvn clean package
    ```
5.  Copy the jar artifact to an S3 bucket
    ```
    BUCKET=some_bucket
    aws s3 cp target/ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar s3://${BUCKET}
    ```
6.  Configure a security role for use by the lambda function.  Get the arn of this role.
7.  All the lambda functions are found inside the jar artifact above.  Create the lambda functions by executing the following commands:
    <pre>
    ROLE_ARN=arn:aws:iam::676593540089:role/service-role/xxx
    <br>
    aws lambda create-function --region us-west-2 --function-name get-user-session-id \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.UserSessionHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>
    aws lambda create-function --region us-west-2 --function-name create-user-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.CreateUserHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>  
    aws lambda create-function --region us-west-2 --function-name grant-user-access-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.GrantUserAccessHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>
    aws lambda create-function --region us-west-2 --function-name create-blog-entry-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.CreateBlogEntryHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>
    aws lambda create-function --region us-west-2 --function-name find-user-blog-entries-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.FindUserBlogEntriesHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>
    aws lambda create-function --region us-west-2 --function-name create-blog-space-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.CreateBlogSpaceHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>
    aws lambda create-function --region us-west-2 --function-name find-blogs-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.FindBlogsHandler \
      --runtime java8 --timeout 15 --memory-size 512
    <br>
    aws lambda create-function --region us-west-2 --function-name approve-blog-entry-handler \
      --code S3Bucket=${BUCKET},S3Key=ec-blog-exam-1.0-SNAPSHOT-jar-with-dependencies.jar,S3ObjectVersion=null \
      --role ${ROLE_ARN} --handler com.epc.blog.ApproveBlogEntryHandler \
      --runtime java8 --timeout 15 --memory-size 512      
    </pre>  
8.  Configure the AWS API Gateway using the AWS Console.
<pre>
a.  Create a new API by clicking 'Create API button'.  
    Fill in the 'API Name', and click 'Create API'.
b.  There are 8 resources in this project, each of which will be associated 
    with the 8 lambdas deployed in step 7.  The succeeding steps will discuss, 
    how to add a resource, and add a method to that resource, and how that 
    resource is configured.  
c.  Create a resource called /blogs/blogEntries{blogEntryId}.  
d.  Add a PUT method to the resource /blogs/blogEntries/{blogEntryId}.
e.  Associate this resource with the 'approve-blog-entry-handler' and allow
    permissions to the said lambda.
f.  Click the 'Integration Request box'.  Expand the 'Mapping Templates' 
    section.  Add a mapping template, specify the Content-Type as 
    application/json.  The mapping template will have a value of:
  
    {
       "pathParam.blogEntryId": "$input.params('blogEntryId')",
       "requestBody": "$util.escapeJavaScript($input.json('$'))"
    }
g.  Click Save.
h.  Refer to document docs/AWS API Gateway Configuration.docx for configuring the other 7 resources and the 
    lambdas associated with them.

</pre>






