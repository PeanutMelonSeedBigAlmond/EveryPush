package moe.peanutmelonseedbigalmond.push.ui.component.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.data.DeviceData

@Composable
fun DeviceListItem(
    thisDeviceFcmToken: String,
    device: DeviceData,
    onDeleteActionSelected: (DeviceData) -> Unit,
    onRenameActionSelected: (DeviceData) -> Unit
) {
    var isMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    isMenuVisible = true
                }
        ) {
            Icon(
                imageVector = Icons.Outlined.PhoneAndroid,
                contentDescription = "Phone icon",
                tint = MaterialTheme.colorScheme.surfaceTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (thisDeviceFcmToken == device.deviceId) device.name + stringResource(id = R.string.this_device) else device.name,
                maxLines = 1
            )
        }
        DropdownMenu(expanded = isMenuVisible, onDismissRequest = { isMenuVisible = false }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.delete)) },
                onClick = {
                    onDeleteActionSelected(device)
                    isMenuVisible = false
                })
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.rename)) },
                onClick = {
                    onRenameActionSelected(device)
                    isMenuVisible = false
                })
        }
    }
}