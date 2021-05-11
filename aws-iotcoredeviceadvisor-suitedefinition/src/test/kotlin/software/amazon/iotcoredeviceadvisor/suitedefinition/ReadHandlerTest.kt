// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.iotcoredeviceadvisor.suitedefinition

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.SuiteDefinitionConfiguration
import software.amazon.awssdk.services.iotdeviceadvisor.model.DeviceUnderTest
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.OperationStatus
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.iotcoredeviceadvisor.suitedefinition.Converter.Companion.convertFromGetSuiteDefinitionResponse
import java.util.function.Function
import kotlin.test.assertFailsWith

class ReadHandlerTest {
    companion object {
        const val TEST_SUITE_DEFINITION_ID = "test-suite-definition-id"
        // Input to ReadHandler should have just suiteDefnitionId
        fun getTestInputModel(): ResourceModel {
            return ResourceModel.builder()
                .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
                .build()
        }
        // After ReadHandler, model should have all properties specified
        fun getTestExpectedModel(): ResourceModel {
            return convertFromGetSuiteDefinitionResponse(testGetSuiteDefinitionResponse)
        }
        val suiteDefinitionConfiguration = SuiteDefinitionConfiguration.builder()
            .suiteDefinitionName("test-suite-definition-name")
            .rootGroup("test-root-group")
            .intendedForQualification(false)
            .devices(listOf(DeviceUnderTest.builder().thingArn("test-thing-arn").thingArn("test-thing-arn").build()))
            .devicePermissionRoleArn("test-device-permission-role-arn").build()
        val testGetSuiteDefinitionResponse = GetSuiteDefinitionResponse.builder().suiteDefinitionConfiguration(suiteDefinitionConfiguration).build()
    }

    private var logger: Logger = mockk()

    private val mockDeviceAdvisorClient: IotDeviceAdvisorClient = mockk()

    private val proxy: AmazonWebServicesClientProxy = mockk()

    private val handler: ReadHandler = ReadHandler()

    @BeforeEach
    fun setup() {
        mockkObject(ClientBuilder)
        every { ClientBuilder.getDeviceAdvisorClient() } returns mockDeviceAdvisorClient
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>())
        } returns testGetSuiteDefinitionResponse
        every {
            logger.log(any())
        } returns Unit
    }

    @Test
    fun handleRequest_SimpleSuccess() {
        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(getTestInputModel())
            .build()
        val response = handler.handleRequest(proxy, request, null, logger)
        Assertions.assertThat(response.status).isEqualTo(OperationStatus.SUCCESS)
        Assertions.assertThat(response.callbackContext).isNull()
        Assertions.assertThat(response.callbackDelaySeconds).isEqualTo(0)
        Assertions.assertThat(response.resourceModel).isEqualTo(getTestExpectedModel())
        Assertions.assertThat(response.resourceModels).isNull()
        Assertions.assertThat(response.message).isNull()
        Assertions.assertThat(response.errorCode).isNull()
    }

    @Test
    fun handleRequest_WithException_ShouldThrowException() {
        val expectedExceptionMsg = "test exception message"
        mockkObject(ExceptionHandler)
        every { ExceptionHandler.handleDeviceAdvisorException(any()) } throws Exception(expectedExceptionMsg)
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>())
        } throws Exception("test message")

        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(getTestInputModel())
            .build()
        val exception = assertFailsWith<Exception> {
            handler.handleRequest(proxy, request, null, logger)
        }
        Assertions.assertThat(exception.message).isEqualTo(expectedExceptionMsg)
    }
}
