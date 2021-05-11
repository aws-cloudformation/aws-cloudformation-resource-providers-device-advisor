// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse
import software.amazon.cloudformation.exceptions.ResourceNotFoundException
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ProgressEvent
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.Converter.Companion.convertFromGetSuiteDefinitionResponse

class ReadHandler : BaseHandler<CallbackContext?>() {
    override fun handleRequest(
        proxy: AmazonWebServicesClientProxy,
        request: ResourceHandlerRequest<ResourceModel>,
        callbackContext: CallbackContext?,
        logger: Logger): ProgressEvent<ResourceModel, CallbackContext?> {

        logger.log("Read SuiteDefinition Request: $request")
        val deviceAdvisorClient = ClientBuilder.getDeviceAdvisorClient()

        val model = request.desiredResourceState
        val suiteDefinitionId = model.suiteDefinitionId
        if (suiteDefinitionId.isNullOrEmpty()) {
            throw ResourceNotFoundException(ResourceModel.TYPE_NAME, null)
        }
        val getSuiteDefinitionRequest = if (model.suiteDefinitionVersion.isNullOrBlank()) {
            GetSuiteDefinitionRequest.builder()
                .suiteDefinitionId(model.suiteDefinitionId).build()
        } else {
            GetSuiteDefinitionRequest.builder()
                .suiteDefinitionId(model.suiteDefinitionId)
                .suiteDefinitionVersion(model.suiteDefinitionVersion).build()
        }

        return try {
            val getSuiteDefinitionResponse: GetSuiteDefinitionResponse = proxy.injectCredentialsAndInvokeV2(getSuiteDefinitionRequest, deviceAdvisorClient::getSuiteDefinition)
            ProgressEvent.defaultSuccessHandler(convertFromGetSuiteDefinitionResponse(getSuiteDefinitionResponse))
        } catch (e: Exception) {
            ExceptionHandler.handleDeviceAdvisorException(e)
        }
    }
}
