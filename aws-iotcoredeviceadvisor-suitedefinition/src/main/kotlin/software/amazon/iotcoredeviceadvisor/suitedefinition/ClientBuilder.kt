package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient

object ClientBuilder {
    fun getDeviceAdvisorClient(): IotDeviceAdvisorClient {
        return IotDeviceAdvisorClient.builder().build()
    }
}
