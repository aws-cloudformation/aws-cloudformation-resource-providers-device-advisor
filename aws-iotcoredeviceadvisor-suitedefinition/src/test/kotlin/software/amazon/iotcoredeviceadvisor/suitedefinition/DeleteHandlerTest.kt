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
import software.amazon.awssdk.services.iotdeviceadvisor.model.DeleteSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.DeleteSuiteDefinitionResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.DeviceUnderTest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.SuiteDefinitionConfiguration
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import software.amazon.cloudformation.proxy.OperationStatus
import java.util.function.Function
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class DeleteHandlerTest {
    companion object {
        const val SUITE_DEFINITION_ARN = "dummySuiteDefinitionArn"
        // Input to DeleteHandler should have suiteDefinitionId specified
        fun getTestModel(): ResourceModel {
            return ResourceModel.builder()
                .suiteDefinitionId(SUITE_DEFINITION_ARN)
                .build()
        }

        val suiteDefinitionConfiguration = SuiteDefinitionConfiguration.builder()
            .suiteDefinitionName("test-suite-definition-name")
            .rootGroup("test-root-group")
            .intendedForQualification(false)
            .devices(listOf(DeviceUnderTest.builder().thingArn("test-thing-arn").thingArn("test-thing-arn").build()))
            .devicePermissionRoleArn("test-device-permission-role-arn").build()
        val getSuiteDefinitionResponse = GetSuiteDefinitionResponse.builder().suiteDefinitionConfiguration(suiteDefinitionConfiguration).build()
    }

    @MockK(relaxed = true)
    lateinit var logger: Logger

    private val mockDeviceAdvisorClient: IotDeviceAdvisorClient = mockk()

    private val proxy: AmazonWebServicesClientProxy = mockk()

    private val handler: DeleteHandler = DeleteHandler()

    @BeforeEach
    fun setup() {
        mockkObject(ClientBuilder)
        every { ClientBuilder.getDeviceAdvisorClient() } returns mockDeviceAdvisorClient
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<DeleteSuiteDefinitionRequest, DeleteSuiteDefinitionResponse>>())
        } returns DeleteSuiteDefinitionResponse.builder().build()
    }

    @Test
    fun handleRequest_SimpleSuccess() {
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>()) } returns getSuiteDefinitionResponse
        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(getTestModel())
            .build()
        val response = handler.handleRequest(proxy, request, null, logger)
        Assertions.assertThat(response.status).isEqualTo(OperationStatus.SUCCESS)
        Assertions.assertThat(response.callbackContext).isNull()
        Assertions.assertThat(response.callbackDelaySeconds).isEqualTo(0)
        Assertions.assertThat(response.resourceModel).isNull()
        Assertions.assertThat(response.resourceModels).isNull()
        Assertions.assertThat(response.message).isNull()
        Assertions.assertThat(response.errorCode).isNull()
    }

    @Test
    fun handleRequest_WithException_ShouldThrowException() {
        val expectedExceptionMsg = "test exception message"
        mockkObject(ExceptionHandler)
        every { ExceptionHandler.handleDeviceAdvisorException(any()) } throws Exception(expectedExceptionMsg)
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>()) } returns getSuiteDefinitionResponse
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<DeleteSuiteDefinitionRequest, DeleteSuiteDefinitionResponse>>())
        } throws Exception("test message")

        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(getTestModel())
            .build()
        val exception = assertFailsWith<Exception> {
            handler.handleRequest(proxy, request, null, logger)
        }
        Assertions.assertThat(exception.message).isEqualTo(expectedExceptionMsg)
    }
}
