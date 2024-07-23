package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.bean.DeviceInfo

@Composable
fun DeviceItem(
    deviceInfo: DeviceInfo,
    isCurrentDevice:Boolean,
    modifier: Modifier = Modifier,
    onRenameRequest: (DeviceInfo) -> Unit = {},
    onRemoveRequest: (DeviceInfo) -> Unit = {},
) {
    var menuShow by remember { mutableStateOf(false) }
    Box {
        Card(modifier = modifier, onClick = { menuShow = true }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.PhoneAndroid, contentDescription = null)
                Text(text = if (isCurrentDevice){
                    deviceInfo.name+"（此设备）"
                }else{
                    deviceInfo.name
                }, modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { menuShow = true }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuShow,
                        onDismissRequest = { menuShow = false },
                    ) {
                        DropdownMenuItem(text = {
                            Text(text = "重命名")
                        }, onClick = {
                            menuShow = false
                            onRenameRequest(deviceInfo)
                        }, leadingIcon = {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        })
                        DropdownMenuItem(text = {
                            Text(text = "删除")
                        }, onClick = {
                            menuShow = false
                            onRemoveRequest(deviceInfo)
                        }, leadingIcon = {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                        })
                    }
                }
            }
        }
    }
}