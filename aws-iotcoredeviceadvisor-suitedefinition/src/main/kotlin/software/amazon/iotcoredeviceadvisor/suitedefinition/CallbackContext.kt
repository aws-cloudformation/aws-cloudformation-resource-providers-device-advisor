// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.amazon.iotcoredeviceadvisor.suitedefinition

import lombok.EqualsAndHashCode
import lombok.Getter
import lombok.Setter
import lombok.ToString
import software.amazon.cloudformation.proxy.StdCallbackContext
import lombok.NoArgsConstructor

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
class CallbackContext : StdCallbackContext()
