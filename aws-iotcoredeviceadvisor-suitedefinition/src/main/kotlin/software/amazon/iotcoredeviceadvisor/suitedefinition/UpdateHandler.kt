package software.amazon.iotcoredeviceadvisor.suitedefinition;

import software.amazon.awssdk.services.iotdeviceadvisor.model.TagResourceRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.UntagResourceRequest
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.UpdateSuiteDefinitionRequest
import software.amazon.cloudformation.exceptions.ResourceNotFoundException
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ProgressEvent
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.ClientBuilder.getDeviceAdvisorClient
import software.amazon.iotcoredeviceadvisor.suitedefinition.ExceptionHandler.handleDeviceAdvisorException
import software.amazon.iotcoredeviceadvisor.suitedefinition.Converter.Companion.convertToSuiteDefinitionConfiguration
import software.amazon.iotcoredeviceadvisor.suitedefinition.Converter.Companion.tagsFromModel

class UpdateHandler : BaseHandler<CallbackContext?>() {
    override fun handleRequest(
        proxy: AmazonWebServicesClientProxy,
        request: ResourceHandlerRequest<ResourceModel>,
        callbackContext: CallbackContext?,
        logger: Logger
    ): ProgressEvent<ResourceModel, CallbackContext?> {
        logger.log("Update SuiteDefinition Request: $request")
        val deviceAdvisorClient = getDeviceAdvisorClient()
        val prevModel = request.previousResourceState
        val model = request.desiredResourceState

        val suiteDefinitionId = prevModel.suiteDefinitionId
        logger.log("Getting SuiteDefinition of id $suiteDefinitionId")

        if (suiteDefinitionId.isNullOrEmpty()) {
            throw ResourceNotFoundException(ResourceModel.TYPE_NAME, null)
        }

        lateinit var suiteDefinition: GetSuiteDefinitionResponse
        try {
            suiteDefinition = getSuiteDefinition(proxy, deviceAdvisorClient, suiteDefinitionId)
        } catch (e: Exception) {
            handleDeviceAdvisorException(e)
        }
        val suiteDefinitionArn = suiteDefinition.suiteDefinitionArn()

        val oldTags = getOldSuiteDefinitionTags(proxy, deviceAdvisorClient, suiteDefinitionId)
        val newTags = tagsFromModel(model.tags)

        val updateRequest: UpdateSuiteDefinitionRequest = UpdateSuiteDefinitionRequest.builder()
            .suiteDefinitionId(suiteDefinitionId)
            .suiteDefinitionConfiguration(convertToSuiteDefinitionConfiguration(model))
            .build()

        try {
            updateTags(oldTags, newTags, suiteDefinitionArn, proxy, deviceAdvisorClient, logger)
            proxy.injectCredentialsAndInvokeV2(updateRequest, deviceAdvisorClient::updateSuiteDefinition)
        } catch (e: Exception) {
            resetTags(oldTags, newTags, suiteDefinitionArn, proxy, deviceAdvisorClient, logger)
            handleDeviceAdvisorException(e)
        }
        return ProgressEvent.defaultSuccessHandler(model)
    }

    private fun updateTags(oldTags: List<Tag>, newTags: Map<String, String>?, suiteDefinitionArn: String,
        proxy: AmazonWebServicesClientProxy,
        deviceAdvisorClient: IotDeviceAdvisorClient,
        logger: Logger) {
        logger.log("Updating tags for suiteDefinition with arn: $suiteDefinitionArn")
        deleteOldMissingTags(oldTags, newTags, suiteDefinitionArn, proxy, deviceAdvisorClient, logger)
        updateOrAddTags(newTags, suiteDefinitionArn, proxy, deviceAdvisorClient, logger)
    }

    private fun resetTags(oldTags: List<Tag>, newTags: Map<String, String>?, suiteDefinitionArn: String,
        proxy: AmazonWebServicesClientProxy,
        deviceAdvisorClient: IotDeviceAdvisorClient,
        logger: Logger) {
        logger.log("Resetting tags to previous status for suiteDefinition with arn: $suiteDefinitionArn")
        val oldTagsMap = oldTags.map { it.key to it.value }.toMap()
        val newTagsList = newTags?.map { Tag.builder().key(it.key).value(it.value).build() }
        if (newTagsList != null) {
            updateTags(newTagsList, oldTagsMap, suiteDefinitionArn, proxy, deviceAdvisorClient, logger)
        } else {
            updateOrAddTags(oldTagsMap, suiteDefinitionArn, proxy, deviceAdvisorClient, logger)
        }
    }

    private fun deleteOldMissingTags(previousTags: List<Tag>?,
        newTags: Map<String, String>?,
        suiteDefinitionArn: String,
        proxy: AmazonWebServicesClientProxy,
        deviceAdvisorClient: IotDeviceAdvisorClient,
        logger: Logger) {
        logger.log("Looking for tags to be deleted")
        val newTagsMap = newTags?.map { it.key to it.value }?.toMap() ?: mapOf<String, String>()
        val keysToDelete = previousTags?.map { x -> x.key }?.minus(newTagsMap.keys)
        if (!keysToDelete.isNullOrEmpty()) {
            logger.log("Untagging tags: [${keysToDelete.toString()}] for suiteDefinition [$suiteDefinitionArn]")
            val untagResourceRequest = UntagResourceRequest.builder().resourceArn(suiteDefinitionArn).tagKeys(keysToDelete).build()
            proxy.injectCredentialsAndInvokeV2(untagResourceRequest, { untagResourceRequest: UntagResourceRequest? ->
                deviceAdvisorClient.untagResource(untagResourceRequest)
            })
        }
    }

    private fun updateOrAddTags(newTags: Map<String, String>?,
        suiteDefinitionArn: String,
        proxy: AmazonWebServicesClientProxy,
        deviceAdvisorClient: IotDeviceAdvisorClient,
        logger: Logger) {
        if (newTags.isNullOrEmpty()) {
            logger.log("No updates or new addition of tags.")
        } else {
            logger.log("Tagging tags: [${newTags.toString()}] for suiteDefinition [$suiteDefinitionArn]")
            val tagRequest = TagResourceRequest.builder().resourceArn(suiteDefinitionArn).tags(newTags).build()
            proxy.injectCredentialsAndInvokeV2(tagRequest, { tagRequest: TagResourceRequest? ->
                deviceAdvisorClient.tagResource(tagRequest)
            })
        }
    }

    private fun getOldSuiteDefinitionTags(proxy: AmazonWebServicesClientProxy, deviceAdvisorClient: IotDeviceAdvisorClient, suiteDefinitionId: String): List<Tag> {
        val getSuiteDefinitionResponse = getSuiteDefinition(proxy, deviceAdvisorClient, suiteDefinitionId)
        return getSuiteDefinitionResponse.tags().map { Tag.builder().key(it.key).value(it.value).build() }
    }

    private fun getSuiteDefinition(proxy: AmazonWebServicesClientProxy, deviceAdvisorClient: IotDeviceAdvisorClient, suiteDefinitionId: String): GetSuiteDefinitionResponse {
        val getSuiteDefinitionRequest = GetSuiteDefinitionRequest.builder().suiteDefinitionId(suiteDefinitionId).build()
        return proxy.injectCredentialsAndInvokeV2(getSuiteDefinitionRequest, deviceAdvisorClient::getSuiteDefinition)
    }
}
