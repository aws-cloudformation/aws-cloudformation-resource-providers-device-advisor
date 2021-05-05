package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.model.DeviceUnderTest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.SuiteDefinitionConfiguration
import software.amazon.iotcoredeviceadvisor.suitedefinition.DeviceUnderTest as ModeledDeviceUnderTest
import software.amazon.iotcoredeviceadvisor.suitedefinition.SuiteDefinitionConfiguration as ModeledSuiteDefinitionConfiguration

class Converter {
    companion object {
        private fun convertToDevicesInRequest(devices: List<ModeledDeviceUnderTest>?): List<DeviceUnderTest>? {
            return devices?.map { dut -> DeviceUnderTest.builder().certificateArn(dut.certificateArn).thingArn(dut.thingArn).build() }
        }

        fun convertToModeledDevices(devices: List<DeviceUnderTest>?): List<ModeledDeviceUnderTest>? {
            return devices?.map { dut -> ModeledDeviceUnderTest.builder().certificateArn(dut.certificateArn()).thingArn(dut.thingArn()).build() }
        }

        fun convertToSuiteDefinitionConfiguration(model: ResourceModel): SuiteDefinitionConfiguration {
            return SuiteDefinitionConfiguration.builder()
                .devicePermissionRoleArn(model.suiteDefinitionConfiguration.devicePermissionRoleArn)
                .intendedForQualification(model.suiteDefinitionConfiguration.intendedForQualification)
                .rootGroup(model.suiteDefinitionConfiguration.rootGroup)
                .suiteDefinitionName(model.suiteDefinitionConfiguration.suiteDefinitionName)
                .devices(convertToDevicesInRequest(model.suiteDefinitionConfiguration.devices))
                .build()
        }

        fun convertFromGetSuiteDefinitionResponse(getSuiteDefinitionResponse: GetSuiteDefinitionResponse): ResourceModel {
            return ResourceModel().apply {
                this.suiteDefinitionId = getSuiteDefinitionResponse.suiteDefinitionId()
                this.suiteDefinitionArn = getSuiteDefinitionResponse.suiteDefinitionArn()
                this.suiteDefinitionVersion = getSuiteDefinitionResponse.suiteDefinitionVersion()
                this.suiteDefinitionConfiguration = getSuiteDefinitionResponse.suiteDefinitionConfiguration().let { suiteDefinitionConfiguration -> ModeledSuiteDefinitionConfiguration().apply {
                    this.intendedForQualification = suiteDefinitionConfiguration.intendedForQualification()
                    this.devicePermissionRoleArn = suiteDefinitionConfiguration.devicePermissionRoleArn()
                    this.suiteDefinitionName = suiteDefinitionConfiguration.suiteDefinitionName()
                    this.rootGroup = suiteDefinitionConfiguration.rootGroup()
                    this.devices = convertToModeledDevices(suiteDefinitionConfiguration.devices())
                }}
            }
        }

        fun tagsFromModel(tags: Set<Tag>?): Map<String, String>? {
            return tags?.map { it.key to it.value }?.toMap() ?: mapOf()
        }
    }
}
