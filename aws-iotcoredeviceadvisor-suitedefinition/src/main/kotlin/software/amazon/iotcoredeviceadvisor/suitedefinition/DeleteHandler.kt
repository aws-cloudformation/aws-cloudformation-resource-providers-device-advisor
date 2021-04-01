package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ProgressEvent
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.ClientBuilder.getDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.DeleteSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse

class DeleteHandler : BaseHandler<CallbackContext?>() {
    override fun handleRequest(
        proxy: AmazonWebServicesClientProxy,
        request: ResourceHandlerRequest<ResourceModel>,
        callbackContext: CallbackContext?,
        logger: Logger
    ): ProgressEvent<ResourceModel, CallbackContext?> {
        logger.log("Delete SuiteDefinition Request: $request")

        val deviceAdvisorClient = getDeviceAdvisorClient()

        val model = request.desiredResourceState
        val suiteDefinitionId = model.suiteDefinitionId

        try {
            getSuiteDefinition(proxy, deviceAdvisorClient, suiteDefinitionId)
        } catch (e: Exception) {
            ExceptionHandler.handleDeviceAdvisorException(e)
        }
        val deleteSuiteDefinitionRequest = DeleteSuiteDefinitionRequest.builder().suiteDefinitionId(suiteDefinitionId).build()

        return try {
            proxy.injectCredentialsAndInvokeV2(deleteSuiteDefinitionRequest, deviceAdvisorClient::deleteSuiteDefinition)
            ProgressEvent.defaultSuccessHandler(null)
        } catch (e: Exception) {
            ExceptionHandler.handleDeviceAdvisorException(e)
        }
    }

    private fun getSuiteDefinition(proxy: AmazonWebServicesClientProxy, deviceAdvisorClient: IotDeviceAdvisorClient, suiteDefinitionId: String): GetSuiteDefinitionResponse {
        val getSuiteDefinitionRequest = GetSuiteDefinitionRequest.builder().suiteDefinitionId(suiteDefinitionId).build()
        return proxy.injectCredentialsAndInvokeV2(getSuiteDefinitionRequest, deviceAdvisorClient::getSuiteDefinition)
    }
}
