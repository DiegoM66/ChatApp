package com.example.chatapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Nosso componente de cabeçalho customizado que substitui o TopAppBar experimental.
 * @param title O texto a ser exibido como título.
 * @param navigationIcon Um ícone opcional para navegação (ex: botão de voltar).
 * @param actions Ações opcionais a serem exibidas no final (ex: botão de busca ou adicionar).
 */
@Composable
fun CustomAppBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp) // Altura bem compacta
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 4.dp), // Padding mínimo
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (navigationIcon != null) {
            Box(
                modifier = Modifier.size(32.dp), // Tamanho fixo para o ícone
                contentAlignment = Alignment.Center
            ) {
                navigationIcon()
            }
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp, // Fonte menor
            fontWeight = FontWeight.Medium, // Peso da fonte mais leve
            modifier = Modifier
                .weight(1f)
                .padding(start = if (navigationIcon != null) 4.dp else 8.dp)
        )
        if (actions != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                content = actions
            )
        }
    }
}