package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.model.CreateSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.CreateSuiteDefinitionResponse
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ProgressEvent
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.ClientBuilder.getDeviceAdvisorClient
import software.amazon.iotcoredeviceadvisor.suitedefinition.ExceptionHandler.handleDeviceAdvisorException

class CreateHandler : BaseHandler<CallbackContext?>() {
    override fun handleRequest(
        proxy: AmazonWebServicesClientProxy,
        request: ResourceHandlerRequest<ResourceModel>,
        callbackContext: CallbackContext?,
        logger: Logger
    ): ProgressEvent<ResourceModel, CallbackContext?> {
        logger.log("Create SuiteDefinition Request: $request")
        val deviceAdvisorClient = getDeviceAdvisorClient()
        val model = request.desiredResourceState
        return try {
            logger.log("Creating new SuiteDefinition with model: $model")
            val createSuiteDefinitionResponse: CreateSuiteDefinitionResponse = proxy.injectCredentialsAndInvokeV2(
                convertToCreateSuiteDefinitionRequest(
                    model
                ), deviceAdvisorClient::createSuiteDefinition
            )
            model.suiteDefinitionId = createSuiteDefinitionResponse.suiteDefinitionId()

            ProgressEvent.defaultSuccessHandler(model)
        } catch (e: Exception) {
            handleDeviceAdvisorException(e)
        }
    }

    private fun convertToCreateSuiteDefinitionRequest(model: ResourceModel): CreateSuiteDefinitionRequest {
        return CreateSuiteDefinitionRequest.builder()
            .suiteDefinitionConfiguration(Converter.convertToSuiteDefinitionConfiguration(model))
            .tags(tagsFromModel(model.tags)).build()
    }

    private fun tagsFromModel(tags: Set<Tag>?): Map<String, String> {
        return tags?.map { it.key to it.value }?.toMap() ?: mapOf()
    }
}
