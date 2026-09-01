package com.example.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

data class NavModuleItem(
    val id: String,
    val shortLabel: String,
    val fullTitle: String,
    val description: String,
    val iconFilled: ImageVector? = null,
    val iconOutlined: ImageVector? = null,
    val customDrawableRes: Int? = null,
    val testTag: String
)

object NavigationConfig {
    const val DEFAULT_NAV_ORDER = "AGENDAMENTOS,CLIENTES,FINANCEIRO,WHATSAPP,NFSE,SERVICOS,PROFISSIONAIS,CONFIGURANCOES"

    val ALL_MODULES = listOf(
        NavModuleItem(
            id = "AGENDAMENTOS",
            shortLabel = "Agenda",
            fullTitle = "Agenda & Atendimentos",
            description = "Grade de horários, agendamentos e encaixes",
            iconFilled = Icons.Default.CalendarMonth,
            iconOutlined = Icons.Outlined.CalendarMonth,
            testTag = "nav_item_agendamentos"
        ),
        NavModuleItem(
            id = "CLIENTES",
            shortLabel = "Clientes",
            fullTitle = "Gestão de Clientes",
            description = "Cadastro de clientes, histórico e aniversariantes",
            iconFilled = Icons.Default.People,
            iconOutlined = Icons.Outlined.People,
            testTag = "nav_item_clientes"
        ),
        NavModuleItem(
            id = "FINANCEIRO",
            shortLabel = "Financeiro",
            fullTitle = "Gestão Financeira / Caixa",
            description = "Fluxo de caixa, receitas, despesas e relatórios",
            iconFilled = Icons.Default.AccountBalanceWallet,
            iconOutlined = Icons.Outlined.AccountBalanceWallet,
            testTag = "nav_item_financeiro"
        ),
        NavModuleItem(
            id = "WHATSAPP",
            shortLabel = "WhatsApp",
            fullTitle = "WhatsApp & Mensagens",
            description = "Confirmações automáticas, lembretes e pós-venda",
            customDrawableRes = R.drawable.ic_whatsapp,
            testTag = "nav_item_whatsapp"
        ),
        NavModuleItem(
            id = "NFSE",
            shortLabel = "NFS-e",
            fullTitle = "Plataforma NFS-e",
            description = "Emissão e gestão de notas fiscais de serviço",
            iconFilled = Icons.Default.ReceiptLong,
            iconOutlined = Icons.Outlined.ReceiptLong,
            testTag = "nav_item_nfse"
        ),
        NavModuleItem(
            id = "SERVICOS",
            shortLabel = "Serviços",
            fullTitle = "Catálogo de Serviços",
            description = "Tabela de procedimentos, preços e duração",
            iconFilled = Icons.Default.ContentCut,
            iconOutlined = Icons.Outlined.ContentCut,
            testTag = "nav_item_servicos"
        ),
        NavModuleItem(
            id = "PROFISSIONAIS",
            shortLabel = "Equipe",
            fullTitle = "Equipe / Profissionais",
            description = "Especialistas, escala de trabalho e comissões",
            iconFilled = Icons.Default.Badge,
            iconOutlined = Icons.Outlined.Badge,
            testTag = "nav_item_profissionais"
        ),
        NavModuleItem(
            id = "CONFIGURANCOES",
            shortLabel = "Ajustes",
            fullTitle = "Configurações do Salão",
            description = "Preferências da empresa, backup e temas visuais",
            iconFilled = Icons.Default.Settings,
            iconOutlined = Icons.Outlined.Settings,
            testTag = "nav_item_configuracoes"
        )
    )

    private val moduleMap = ALL_MODULES.associateBy { it.id }

    fun getModule(id: String): NavModuleItem {
        return moduleMap[id] ?: NavModuleItem(
            id = id,
            shortLabel = id,
            fullTitle = id,
            description = "",
            iconFilled = Icons.Default.Dashboard,
            testTag = "nav_item_$id"
        )
    }

    /**
     * Parses the comma-separated order string and guarantees all modules are present without duplicates.
     */
    fun parseNavOrder(orderString: String?): List<NavModuleItem> {
        val parsedIds = (orderString ?: DEFAULT_NAV_ORDER)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() && moduleMap.containsKey(it) }
            .distinct()

        val missingIds = ALL_MODULES.map { it.id }.filter { !parsedIds.contains(it) }
        val fullIds = parsedIds + missingIds
        return fullIds.mapNotNull { moduleMap[it] }
    }

    fun serializeNavOrder(modules: List<NavModuleItem>): String {
        return modules.joinToString(",") { it.id }
    }
}
