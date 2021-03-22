package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.model.InternalServerException
import software.amazon.awssdk.services.iotdeviceadvisor.model.ResourceNotFoundException
import software.amazon.awssdk.services.iotdeviceadvisor.model.ConflictException
import software.amazon.awssdk.services.iotdeviceadvisor.model.ValidationException
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException
import software.amazon.cloudformation.exceptions.ResourceNotFoundException as CfnResourceNotFoundException
import software.amazon.cloudformation.proxy.ProgressEvent

object ExceptionHandler {

    /*
        Handle exception and return a ProgressEvent (most likely FAILED type) or throw an exception
        Throwing an exception that the framework recognizes is equivalent to creating a FAILED ProgressEvent.
     */
    fun handleDeviceAdvisorException(e: Exception): ProgressEvent<ResourceModel, CallbackContext?> {
        throw when (e) {
            is ConflictException -> CfnAlreadyExistsException(e)
            is ValidationException -> CfnInvalidRequestException(e)
            is ResourceNotFoundException -> CfnResourceNotFoundException(e)
            is InternalServerException -> CfnServiceInternalErrorException(e)
            else -> e
        }
    }
}
