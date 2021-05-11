// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.iotcoredeviceadvisor.suitedefinition

import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient

object ClientBuilder {
    fun getDeviceAdvisorClient(): IotDeviceAdvisorClient {
        return IotDeviceAdvisorClient.builder().build()
    }
}
