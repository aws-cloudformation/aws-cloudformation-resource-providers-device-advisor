# AWS::IoTCoreDeviceAdvisor::SuiteDefinition

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::IoTCoreDeviceAdvisor::SuiteDefinition",
    "Properties" : {
        "<a href="#suitedefinitionconfiguration" title="SuiteDefinitionConfiguration">SuiteDefinitionConfiguration</a>" : <i><a href="suitedefinitionconfiguration.md">SuiteDefinitionConfiguration</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::IoTCoreDeviceAdvisor::SuiteDefinition
Properties:
    <a href="#suitedefinitionconfiguration" title="SuiteDefinitionConfiguration">SuiteDefinitionConfiguration</a>: <i><a href="suitedefinitionconfiguration.md">SuiteDefinitionConfiguration</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### SuiteDefinitionConfiguration

_Required_: Yes

_Type_: <a href="suitedefinitionconfiguration.md">SuiteDefinitionConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the SuiteDefinitionId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### SuiteDefinitionId

The unique identifier for the suite definition.

#### SuiteDefinitionArn

The Amazon Resource name for the suite definition.

#### SuiteDefinitionVersion

The suite definition version of a test suite.

