# AWS::IoTCoreDeviceAdvisor::SuiteDefinition

#### Building and testing the project
There are a couple options for building and testing the project.

1. "mvn clean package" - this will build clean and build the project.
2. "cfn submit -v --region <desired_region> - This will deploy the resource as a private resource to your aws account.  This may take a couple minutes.


##### Files:

1. `aws-iotcoredeviceadvisor-suitedefinition.json` - This file contains the schema definition for the SuiteDefinition resource.  It also drives auto generated classes from rpdk.
2. Resource handlers live in `src/main/kotlin/software/amazon/iotcoredeviceadvisor/suitedefinition`


#### How to test locally
2 ways of testing:
1. Sam local invoke
2. Deploy stack in your local (cfn submit and aws cfn create-stack)

Please don't modify files under `target/generated-sources/rpdk`, as they will be
automatically overwritten.
