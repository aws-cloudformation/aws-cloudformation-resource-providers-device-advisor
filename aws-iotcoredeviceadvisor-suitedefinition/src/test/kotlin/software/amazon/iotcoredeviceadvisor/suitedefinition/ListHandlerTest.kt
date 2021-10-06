// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.iotcoredeviceadvisor.suitedefinition

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.ListSuiteDefinitionsRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.ListSuiteDefinitionsResponse
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.cloudformation.proxy.OperationStatus
import java.util.function.Function
import kotlin.test.assertFailsWith
import software.amazon.awssdk.services.iotdeviceadvisor.model.SuiteDefinitionInformation

@ExtendWith(MockKExtension::class)
class ListHandlerTest {
    companion object {

        private val suiteDef1: SuiteDefinitionInformation = SuiteDefinitionInformation.builder()
                .suiteDefinitionId("suiteId1")
                .suiteDefinitionName("Test")
                .intendedForQualification(true)
                .defaultDevices(listOf())
                .build()
        private val suiteDef2: SuiteDefinitionInformation = SuiteDefinitionInformation.builder()
                .suiteDefinitionId("suiteId2")
                .suiteDefinitionName("Test2")
                .intendedForQualification(false)
                .defaultDevices(listOf())
                .build()

        val listSuiteDefinitionsResponse: ListSuiteDefinitionsResponse = ListSuiteDefinitionsResponse.builder().suiteDefinitionInformationList(listOf(suiteDef1, suiteDef2)).nextToken("token2").build()

    }

    @MockK(relaxed = true)
    lateinit var logger: Logger

    private val mockDeviceAdvisorClient: IotDeviceAdvisorClient = mockk()

    private val proxy: AmazonWebServicesClientProxy = mockk()

    private val handler: ListHandler = ListHandler()

    @BeforeEach
    fun setup() {
        mockkObject(ClientBuilder)
        every { ClientBuilder.getDeviceAdvisorClient() } returns mockDeviceAdvisorClient
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<ListSuiteDefinitionsRequest, ListSuiteDefinitionsResponse>>())
        } returns ListSuiteDefinitionsResponse.builder().build()
    }

    @Test
    fun handleRequest_SimpleSuccess() {

        val model1: ResourceModel = ResourceModel.builder()
                .suiteDefinitionId("suiteId1")
                .suiteDefinitionConfiguration(software.amazon.iotcoredeviceadvisor.suitedefinition.SuiteDefinitionConfiguration.builder()
                        .suiteDefinitionName("Test")
                        .rootGroup(null)
                        .intendedForQualification(true)
                        .devices(emptyList())
                        .devicePermissionRoleArn(null).build()).build()

        val model2: ResourceModel = ResourceModel.builder()
                .suiteDefinitionId("suiteId2")
                .suiteDefinitionConfiguration(software.amazon.iotcoredeviceadvisor.suitedefinition.SuiteDefinitionConfiguration.builder()
                        .suiteDefinitionName("Test2")
                        .rootGroup(null)
                        .intendedForQualification(false)
                        .devices(emptyList())
                        .devicePermissionRoleArn(null).build()).build()

        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<ListSuiteDefinitionsRequest, ListSuiteDefinitionsResponse>>()) } returns listSuiteDefinitionsResponse

        val request = ResourceHandlerRequest.builder<ResourceModel>()
                .nextToken("token")
                .build()

        val response = handler.handleRequest(proxy, request, null, logger)

        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.status).isEqualTo(OperationStatus.SUCCESS)
        Assertions.assertThat(response.callbackContext).isNull()
        Assertions.assertThat(response.callbackDelaySeconds).isEqualTo(0)
        Assertions.assertThat(response.resourceModel).isNull()
        Assertions.assertThat(response.resourceModels).containsAll(listOf(model1, model2))
        Assertions.assertThat(response.nextToken).isEqualTo("token2")
        Assertions.assertThat(response.message).isNull()
        Assertions.assertThat(response.errorCode).isNull()

    }

    @Test
    fun handleRequest_WithException_ShouldThrowException() {
        val expectedExceptionMsg = "test exception message"
        mockkObject(ExceptionHandler)
        every { ExceptionHandler.handleDeviceAdvisorException(any()) } throws Exception(expectedExceptionMsg)
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<ListSuiteDefinitionsRequest, ListSuiteDefinitionsResponse>>())
        } throws Exception("test message")

        val request = ResourceHandlerRequest.builder<ResourceModel>()
                .nextToken("token")
                .build()

        val exception = assertFailsWith<Exception> {
            handler.handleRequest(proxy, request, null, logger)
        }
        Assertions.assertThat(exception.message).isEqualTo(expectedExceptionMsg)
    }
}
