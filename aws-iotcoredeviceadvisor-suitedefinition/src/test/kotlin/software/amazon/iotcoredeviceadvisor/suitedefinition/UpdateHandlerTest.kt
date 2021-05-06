package software.amazon.iotcoredeviceadvisor.suitedefinition

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.GetSuiteDefinitionResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.InternalServerException
import software.amazon.awssdk.services.iotdeviceadvisor.model.ResourceNotFoundException
import software.amazon.awssdk.services.iotdeviceadvisor.model.SuiteDefinitionConfiguration
import software.amazon.awssdk.services.iotdeviceadvisor.model.TagResourceRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.TagResourceResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.UntagResourceRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.UntagResourceResponse
import software.amazon.awssdk.services.iotdeviceadvisor.model.UpdateSuiteDefinitionRequest
import software.amazon.awssdk.services.iotdeviceadvisor.model.UpdateSuiteDefinitionResponse
import software.amazon.iotcoredeviceadvisor.suitedefinition.SuiteDefinitionConfiguration as ModeledSuiteDefinitionConfiguration
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy
import software.amazon.cloudformation.proxy.Logger
import software.amazon.cloudformation.proxy.OperationStatus
import software.amazon.cloudformation.proxy.ResourceHandlerRequest
import java.util.function.Function
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class UpdateHandlerTest {

    @MockK
    lateinit var proxy: AmazonWebServicesClientProxy

    @MockK(relaxed = true)
    lateinit var logger: Logger

    private val TEST_SUITE_DEFINITION_ID = "test-suite-definition-id"
    private val TEST_SUITE_DEFINITION_ARN = "test-suite-definition-arn"
    val device1 = DeviceUnderTest.builder().certificateArn("test-cert-arn-1").thingArn("test-thing-arn-1").build()
    val device2 = DeviceUnderTest.builder().certificateArn("test-cert-arn-2").thingArn("test-thing-arn-2").build()
    val listOfDevice = listOf(device1)
    val listOfDevices = listOf(device1, device2)
    private val suiteDefinitionConfiguration1 = ModeledSuiteDefinitionConfiguration.builder()
        .suiteDefinitionName("test-suite-definition-name-1")
        .devicePermissionRoleArn("test-suite-definition-arn-1")
        .intendedForQualification(false).rootGroup("test-root-group-1")
        .devices(listOfDevice)
        .build()
    private val suiteDefinitionConfiguration2 = ModeledSuiteDefinitionConfiguration.builder()
        .suiteDefinitionName("test-suite-definition-name-2")
        .devicePermissionRoleArn("test-suite-definition-arn-2")
        .intendedForQualification(false).rootGroup("test-root-group-2")
        .devices(listOfDevices)
        .build()

    private fun createTestResourceModel(): ResourceModel {
        val tag1 = Tag.builder().key("Key1").value("Value1").build()
        val listOfTags = setOf(tag1)
        return ResourceModel.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionConfiguration(suiteDefinitionConfiguration1)
            .suiteDefinitionArn("test-suite-definition-arn")
            .tags(listOfTags)
            .build()
    }

    private fun createNewTestResourceModel(): ResourceModel {
        val tag1 = Tag.builder().key("Key1").value("Value1").build()
        val tag2 = Tag.builder().key("Key2").value("Value2").build()
        val listOfTags = setOf(tag1, tag2)
        return ResourceModel.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionConfiguration(suiteDefinitionConfiguration2)
            .tags(listOfTags)
            .build()
    }

    @BeforeEach
    fun setup() = MockKAnnotations.init(this)

    @Test
    fun handleRequest_NonExistingSuiteDefinition() {
        val handler = UpdateHandler()
        val desiredModel = createTestResourceModel()
        val previousModel = createTestResourceModel()
        every { proxy.injectCredentialsAndInvokeV2(any(), ofType<Function<UpdateSuiteDefinitionRequest, UpdateSuiteDefinitionResponse>>()) } throws ResourceNotFoundException.builder().message("Not found").build()
        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build()
        assertFailsWith<Exception> {
            handler.handleRequest(proxy, request, null, logger)
        }
    }

    @Test
    fun handleRequest_UpdateSuiteDefinition_MissingTags() {
        val handler = UpdateHandler()
        val desiredModel = ResourceModel.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionConfiguration(suiteDefinitionConfiguration2)
            .suiteDefinitionArn("test-suite-definition-arn")
            .build()

        val getSuiteDefinitionResponse = GetSuiteDefinitionResponse.builder()
            .suiteDefinitionConfiguration(SuiteDefinitionConfiguration.builder().build())
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionArn(TEST_SUITE_DEFINITION_ARN)
            .build()
        val updateSuiteDefinitionResponse = UpdateSuiteDefinitionResponse.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .build()

        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>()) } returns getSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UpdateSuiteDefinitionRequest, UpdateSuiteDefinitionResponse>>()) } returns updateSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<TagResourceRequest, TagResourceResponse>>()) } returns null

        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(desiredModel)
            .previousResourceState(desiredModel)
            .build()
        val response = handler.handleRequest(proxy, request, null, logger)
        assertNotNull(response)
        assertEquals(OperationStatus.SUCCESS, response.status)
        assertEquals(0, response.callbackDelaySeconds)
        assertNotNull(response.resourceModel)
        assertEquals(TEST_SUITE_DEFINITION_ID, response.resourceModel.suiteDefinitionId)
        assertEquals(desiredModel.suiteDefinitionConfiguration.devicePermissionRoleArn, response.resourceModel.suiteDefinitionConfiguration.devicePermissionRoleArn)
        assertEquals(desiredModel.suiteDefinitionConfiguration.suiteDefinitionName, response.resourceModel.suiteDefinitionConfiguration.suiteDefinitionName)
        assertEquals(desiredModel.suiteDefinitionConfiguration.rootGroup, response.resourceModel.suiteDefinitionConfiguration.rootGroup)
    }

    @Test
    fun handleRequest_UpdateSuiteDefinition_TagsDrift() {
        val handler = UpdateHandler()
        val desiredModel = ResourceModel.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionConfiguration(suiteDefinitionConfiguration1)
            .suiteDefinitionArn("test-suite-definition-arn")
            .build()

        val previousModel = ResourceModel.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionConfiguration(suiteDefinitionConfiguration1)
            .suiteDefinitionArn("test-suite-definition-arn")
            .tags(setOf(Tag.builder().key("Key1").value("Value2").build()))
            .build()

        val getSuiteDefinitionResponse = GetSuiteDefinitionResponse.builder()
            .suiteDefinitionConfiguration(SuiteDefinitionConfiguration.builder().build())
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionArn(TEST_SUITE_DEFINITION_ARN)
            .build()
        val updateSuiteDefinitionResponse = UpdateSuiteDefinitionResponse.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .build()

        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>()) } returns getSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UpdateSuiteDefinitionRequest, UpdateSuiteDefinitionResponse>>()) } returns updateSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<TagResourceRequest, TagResourceResponse>>()) } returns null
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UntagResourceRequest, UntagResourceResponse>>()) } returns null

        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build()
        val response = handler.handleRequest(proxy, request, null, logger)
        assertNotNull(response)
        assertEquals(OperationStatus.SUCCESS, response.status)
        assertEquals(0, response.callbackDelaySeconds)
        assertNotNull(response.resourceModel)
        assertEquals(TEST_SUITE_DEFINITION_ID, response.resourceModel.suiteDefinitionId)
        assertEquals(desiredModel.suiteDefinitionConfiguration.rootGroup, response.resourceModel.suiteDefinitionConfiguration.rootGroup)
        assertEquals(desiredModel.suiteDefinitionConfiguration.suiteDefinitionName, response.resourceModel.suiteDefinitionConfiguration.suiteDefinitionName)
        assertEquals(desiredModel.suiteDefinitionConfiguration.devicePermissionRoleArn, response.resourceModel.suiteDefinitionConfiguration.devicePermissionRoleArn)
    }

    @Test
    fun handleRequest_UpdateSuiteDefinition_UpdatedSuiteDefinitionName() {
        val handler = UpdateHandler()
        val previousModel = createTestResourceModel()
        previousModel.suiteDefinitionConfiguration.suiteDefinitionName = "test-suite-definition-name-1"
        val desiredModel = createTestResourceModel()
        desiredModel.suiteDefinitionConfiguration.suiteDefinitionName = "test-suite-definition-name-2"

        desiredModel.tags = setOf(Tag.builder().key("Key1").value("Value1").build())
        val getSuiteDefinitionResponse = GetSuiteDefinitionResponse.builder()
            .suiteDefinitionConfiguration(SuiteDefinitionConfiguration.builder().build())
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionArn(TEST_SUITE_DEFINITION_ARN)
            .build()
        val updateSuiteDefinitionResponse = UpdateSuiteDefinitionResponse.builder()
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .build()

        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>()) } returns getSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UpdateSuiteDefinitionRequest, UpdateSuiteDefinitionResponse>>()) } returns updateSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<TagResourceRequest, TagResourceResponse>>()) } returns null

        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build()
        val response = handler.handleRequest(proxy, request, null, logger)
        assertNotNull(response)
        assertEquals(OperationStatus.SUCCESS, response.status)
    }

    @Test
    fun handleRequest_UpdateSuiteDefinition_UpdatedFailed() {
        val handler = UpdateHandler()
        val previousModel = createTestResourceModel()
        previousModel.suiteDefinitionConfiguration.suiteDefinitionName = "test-suite-definition-name-1"
        val desiredModel = createNewTestResourceModel()
        desiredModel.suiteDefinitionConfiguration.suiteDefinitionName = "test-suite-definition-name-2"

        val getSuiteDefinitionResponse = GetSuiteDefinitionResponse.builder()
            .suiteDefinitionConfiguration(SuiteDefinitionConfiguration.builder().build())
            .suiteDefinitionId(TEST_SUITE_DEFINITION_ID)
            .suiteDefinitionArn(TEST_SUITE_DEFINITION_ARN)
            .tags(mapOf("Key1" to "Value1"))
            .build()

        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<GetSuiteDefinitionRequest, GetSuiteDefinitionResponse>>()) } returns getSuiteDefinitionResponse
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UpdateSuiteDefinitionRequest, UpdateSuiteDefinitionResponse>>()) } throws InternalServerException.builder().build()
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<TagResourceRequest, TagResourceResponse>>()) } returns null
        every { proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UntagResourceRequest, UntagResourceResponse>>()) } returns null
        val request = ResourceHandlerRequest.builder<ResourceModel>()
            .desiredResourceState(desiredModel)
            .previousResourceState(previousModel)
            .build()
        assertFailsWith<Exception> {
            handler.handleRequest(proxy, request, null, logger)
        }
        verify {
            proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<TagResourceRequest, TagResourceResponse>>())
            proxy.injectCredentialsAndInvokeV2(ofType(), ofType<Function<UntagResourceRequest, UntagResourceResponse>>())
        }
    }
}
