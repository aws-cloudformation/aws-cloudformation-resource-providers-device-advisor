# AWS::IoTCoreDeviceAdvisor::SuiteDefinition SuiteDefinitionConfiguration

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#devicepermissionrolearn" title="DevicePermissionRoleArn">DevicePermissionRoleArn</a>" : <i>String</i>,
    "<a href="#devices" title="Devices">Devices</a>" : <i>[ <a href="deviceundertest.md">DeviceUnderTest</a>, ... ]</i>,
    "<a href="#intendedforqualification" title="IntendedForQualification">IntendedForQualification</a>" : <i>Boolean</i>,
    "<a href="#rootgroup" title="RootGroup">RootGroup</a>" : <i>String</i>,
    "<a href="#suitedefinitionname" title="SuiteDefinitionName">SuiteDefinitionName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#devicepermissionrolearn" title="DevicePermissionRoleArn">DevicePermissionRoleArn</a>: <i>String</i>
<a href="#devices" title="Devices">Devices</a>: <i>
      - <a href="deviceundertest.md">DeviceUnderTest</a></i>
<a href="#intendedforqualification" title="IntendedForQualification">IntendedForQualification</a>: <i>Boolean</i>
<a href="#rootgroup" title="RootGroup">RootGroup</a>: <i>String</i>
<a href="#suitedefinitionname" title="SuiteDefinitionName">SuiteDefinitionName</a>: <i>String</i>
</pre>

## Properties

#### DevicePermissionRoleArn

The device permission role arn of the test suite.

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Devices

The devices being tested in the test suite

_Required_: No

_Type_: List of <a href="deviceundertest.md">DeviceUnderTest</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IntendedForQualification

Whether the tests are intended for qualification in a suite.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RootGroup

The root group of the test suite.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SuiteDefinitionName

The Name of the suite definition.

_Required_: Yes 

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
