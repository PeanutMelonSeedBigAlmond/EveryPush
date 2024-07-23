package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.bean.MessageGroup

@Composable
fun MessageGroupItem(
    groupId: String?,
    name: String,
    modifier: Modifier = Modifier,
    onClick: (String?) -> Unit = {},
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(groupId) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = name[0].toString(),
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
        Text(
            text = name,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun MessageGroupItem(
    messageGroup: MessageGroup,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = messageGroup.name[0].toString(),
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
        Text(
            text = messageGroup.name,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun MessageGroupItemWithOptions(
    messageGroup: MessageGroup,
    modifier: Modifier = Modifier,
    onClick: (MessageGroup) -> Unit = {},
    onDelete: (messageGroup: MessageGroup) -> Unit = {},
    onRename: (messageGroup: MessageGroup) -> Unit = {},
) {
    var menuShow by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(messageGroup) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MessageGroupItem(
            messageGroup = messageGroup,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        Box {
            IconButton(onClick = { menuShow = true }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = menuShow, onDismissRequest = { menuShow = false }) {
                DropdownMenuItem(
                    text = { Text(text = "删除") },
                    onClick = {
                        menuShow = false
                        onDelete(messageGroup)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null
                        )
                    })
                DropdownMenuItem(
                    text = { Text(text = "重命名") },
                    onClick = {
                        menuShow = false
                        onRename(messageGroup)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    }
                )
            }
        }
    }
}