// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ProgressEvent
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.ClientBuilder.getDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.ListSuiteDefinitionsRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.ListSuiteDefinitionsResponse
import software.amazon.cloudformation.proxy.OperationStatus
import software.amazon.iotcoredeviceadvisor.suitedefinition.Converter.Companion.convertFromListSuiteDefinitionResponse



class ListHandler : BaseHandler<CallbackContext?>() {
    override fun handleRequest(
            proxy: AmazonWebServicesClientProxy,
            request: ResourceHandlerRequest<ResourceModel>,
            callbackContext: CallbackContext?,
            logger: Logger
    ): ProgressEvent<ResourceModel, CallbackContext?> {
        logger.log("List SuiteDefinitions Request: $request")

        val nextToken = request.nextToken
        val deviceAdvisorClient = getDeviceAdvisorClient()

        return try {

            val listSuiteDefinitionsRequest = ListSuiteDefinitionsRequest.builder().maxResults(50)
                    .nextToken(nextToken)
                    .build();

            val response: ListSuiteDefinitionsResponse = proxy.injectCredentialsAndInvokeV2(listSuiteDefinitionsRequest,
                    deviceAdvisorClient::listSuiteDefinitions)

            ProgressEvent.builder<ResourceModel, CallbackContext>()
                    .status(OperationStatus.SUCCESS)
                    .resourceModels(convertFromListSuiteDefinitionResponse(response))
                    .nextToken(response.nextToken())
                    .build()

        } catch (e: Exception) {
            ExceptionHandler.handleDeviceAdvisorException(e)
        }
    }
}
