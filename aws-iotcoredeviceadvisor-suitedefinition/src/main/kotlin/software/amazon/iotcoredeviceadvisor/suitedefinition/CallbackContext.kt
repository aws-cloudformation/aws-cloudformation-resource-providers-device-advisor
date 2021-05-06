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
