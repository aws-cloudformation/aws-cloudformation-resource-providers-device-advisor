package software.amazon.iotcoredeviceadvisor.suitedefinition

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient
import software.amazon.awssdk.services.iotdeviceadvisor.model.CreateSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.CreateSuiteDefinitionResponse
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.OperationStatus
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import java.util.function.Function
import kotlin.test.assertFailsWith

class CreateHandlerTest {

    companion object {
        const val TEST_SUITE_DEFINITION_ID = "test-suite-definition-id"
        // Input to CreateHandler should have no suiteDefinitionId, all properties specified
        fun getTestInputModel(): ResourceModel {
            val suiteDefinitionConfiguration = SuiteDefinitionConfiguration.builder()
                .suiteDefinitionName("dummySuiteDefinitionName")
                .devicePermissionRoleArn("dummyDevicePermissionRoleArn")
                .intendedForQualification(false).rootGroup("dummyRootGroup")
                .devices(listOf(DeviceUnderTest()))
                .build()
            return ResourceModel.builder()
                .suiteDefinitionConfiguration(suiteDefinitionConfiguration)
                .suiteDefinitionVersion("v1")
                .build()
        }
        // After CreateHandler, model should have suiteDefinitionId assigned
        fun getTestExpectedModel(): ResourceModel {
            return getTestInputModel().apply { this.suiteDefinitionId = TEST_SUITE_DEFINITION_ID }
        }
    }

    private var logger: Logger = mockk()

    private val mockDeviceAdvisorClient: IotDeviceAdvisorClient = mockk()

    private val proxy: AmazonWebServicesClientProxy = mockk()

    private val handler: CreateHandler = CreateHandler()

    @BeforeEach
    fun setup() {
        mockkObject(ClientBuilder)
        every { ClientBuilder.getDeviceAdvisorClient() } returns mockDeviceAdvisorClient
        every {
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<CreateSuiteDefinitionRequest, CreateSuiteDefinitionResponse>>())
        } returns CreateSuiteDefinitionResponse.builder().suiteDefinitionId(TEST_SUITE_DEFINITION_ID).build()
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
            proxy.injectCredentialsAndInvokeV2(any(), any<Function<CreateSuiteDefinitionRequest, CreateSuiteDefinitionResponse>>())
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
