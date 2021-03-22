package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ProgressEvent
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.ClientBuilder.getDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.DeleteSuiteDefinitionRequest

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
        val deleteSuiteDefinitionRequest = DeleteSuiteDefinitionRequest.builder().suiteDefinitionId(model.suiteDefinitionId).build()

        return try {
            proxy.injectCredentialsAndInvokeV2(deleteSuiteDefinitionRequest, deviceAdvisorClient::deleteSuiteDefinition)
            ProgressEvent.defaultSuccessHandler(null)
        } catch (e: Exception) {
            ExceptionHandler.handleDeviceAdvisorException(e)
        }
    }
}
