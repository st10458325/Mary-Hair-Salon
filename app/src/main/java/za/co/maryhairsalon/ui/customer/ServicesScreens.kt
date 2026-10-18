package za.co.maryhairsalon.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import za.co.maryhairsalon.data.model.Service
import za.co.maryhairsalon.data.repository.CategoryWithCount
import za.co.maryhairsalon.ui.CollectMessages
import za.co.maryhairsalon.ui.components.EmptyState
import za.co.maryhairsalon.ui.components.GlassCard
import za.co.maryhairsalon.ui.components.GradientButton
import za.co.maryhairsalon.ui.components.LoadingBox
import za.co.maryhairsalon.ui.components.SearchField
import za.co.maryhairsalon.ui.components.SectionHeader
import za.co.maryhairsalon.ui.components.formatZar
import za.co.maryhairsalon.ui.theme.Brand

/** Services tab: hero, search, category grid and deals (mirrors services.php + the home page). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesTab(
    vm: ServicesViewModel,
    snackbar: SnackbarHostState,
    onCategory: (String) -> Unit,
    onBook: (serviceId: String) -> Unit,
) {
    val query by vm.query.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val deals by vm.deals.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    CollectMessages(vm.messages, snackbar)

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = vm::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Column {
                    Text("MARY HAIR SALON", style = MaterialTheme.typography.labelSmall, color = Brand.OrangeLight)
                    Spacer(Modifier.height(6.dp))
                    Text("Hair That Wears The Crown", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(6.dp))
                    Text("Braids, cuts, kids styles and wig installs — booked in minutes.", color = Brand.TextDim)
                    Spacer(Modifier.height(14.dp))
                    SearchField(query, vm::onQuery)
                }
            }

            if (query.isNotBlank()) {
                item { Text("${results.size} result${if (results.size == 1) "" else "s"} for “$query”", color = Brand.TextDim) }
                if (results.isEmpty()) {
                    item { EmptyState("No matches", "Try another word, like “braids”, “cut” or “colour”.") }
                }
                items(results, key = { it.id }) { ServiceCard(it, onBook = { onBook(it.id) }) }
            } else {
                val cats = categories
                when {
                    cats == null -> item { Box(Modifier.height(200.dp)) { LoadingBox() } }
                    cats.isEmpty() -> item {
                        EmptyState("No services yet", "Pull down to refresh. If this stays empty, check your internet connection.")
                    }
                    else -> {
                        item { SectionHeader("Our Services", "Choose a category") }
                        items(cats.chunked(2), key = { row -> row.first().category.id }) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { CategoryTile(it, Modifier.weight(1f)) { onCategory(it.category.id) } }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                if (deals.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        SectionHeader("Limited Time", "Discounted Styles & Deals")
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            deals.forEach { DealCard(it) { onBook(it.id) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(item: CategoryWithCount, modifier: Modifier, onClick: () -> Unit) {
    GlassCard(modifier.height(150.dp), onClick = onClick) {
        CategoryImage(item.category.image, item.category.label, Modifier.size(48.dp))
        Spacer(Modifier.weight(1f))
        Text(item.category.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (item.category.description.isNotBlank()) {
            Text(item.category.description, style = MaterialTheme.typography.bodySmall, color = Brand.TextDim, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Text(
            "${item.serviceCount} service${if (item.serviceCount == 1) "" else "s"}",
            style = MaterialTheme.typography.labelMedium,
            color = Brand.OrangeLight,
        )
    }
}

/** Category photo from its image URL; shows a person avatar when there is no URL or it fails to load. */
@Composable
private fun CategoryImage(url: String, label: String, modifier: Modifier = Modifier) {
    val avatar: @Composable () -> Unit = {
        Box(Modifier.fillMaxSize().background(Brand.OrangeSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Brand.OrangeLight, modifier = Modifier.fillMaxSize(0.6f))
        }
    }
    Box(modifier.clip(CircleShape)) {
        if (url.isBlank()) {
            avatar()
        } else {
            SubcomposeAsyncImage(
                model = url.trim(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { avatar() },
                error = { avatar() },
            )
        }
    }
}

@Composable
private fun DealCard(service: Service, onClick: () -> Unit) {
    GlassCard(Modifier.width(220.dp), onClick = onClick) {
        Text(
            "Save ${service.discount.toInt()}%",
            modifier = Modifier.clip(RoundedCornerShape(50)).background(Brand.Success.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp),
            color = Brand.Success,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.height(10.dp))
        Text(service.title, style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(formatZar(service.price), color = Brand.TextFaint, textDecoration = TextDecoration.LineThrough, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(6.dp))
            Text(formatZar(service.discountedPrice), color = Brand.OrangeLight, fontWeight = FontWeight.Bold)
        }
    }
}

/** A bookable service (website: service-card). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceCard(service: Service, onBook: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Text(service.title, style = MaterialTheme.typography.titleLarge)
        if (service.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(service.description, color = Brand.TextDim)
        }
        if (service.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                service.tags.forEach { tag ->
                    Text(
                        tag,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Brand.Glass).padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Brand.TextDim,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (service.discount > 0) {
                    Text(formatZar(service.price), color = Brand.TextFaint, textDecoration = TextDecoration.LineThrough, style = MaterialTheme.typography.bodySmall)
                    Text("${formatZar(service.discountedPrice)}  (-${service.discount.toInt()}%)", color = Brand.OrangeLight, fontWeight = FontWeight.Bold)
                } else {
                    Text(formatZar(service.price), color = Brand.OrangeLight, fontWeight = FontWeight.Bold)
                }
            }
            GradientButton("Book Now", onBook, Modifier.width(130.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(vm: CategoryViewModel, onBack: () -> Unit, onBook: (String) -> Unit) {
    val category by vm.category.collectAsStateWithLifecycle()
    val services by vm.services.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        containerColor = Brand.Bg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(category?.label ?: "Services") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand.Bg),
            )
        },
    ) { padding ->
        val list = services
        when {
            list == null -> LoadingBox(Modifier.padding(padding))
            list.isEmpty() -> EmptyState("Nothing here yet", "This category has no services at the moment.", Modifier.padding(padding))
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                category?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    item { Text(desc, color = Brand.TextDim) }
                }
                items(list, key = { it.id }) { ServiceCard(it, onBook = { onBook(it.id) }) }
            }
        }
    }
}
