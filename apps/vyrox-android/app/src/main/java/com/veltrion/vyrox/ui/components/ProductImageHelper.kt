package com.veltrion.vyrox.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.veltrion.vyrox.R

fun getProductDrawableRes(categoryName: String? = null, title: String? = null, brandName: String? = null): Int {
    val cat = (categoryName ?: "").lowercase()
    val t = (title ?: "").lowercase()
    val b = (brandName ?: "").lowercase()

    return when {
        cat.contains("laptop") || t.contains("macbook") || t.contains("laptop") || t.contains("xps") -> R.drawable.ic_product_laptop
        cat.contains("mobile") || cat.contains("phone") || t.contains("iphone") || t.contains("galaxy") || t.contains("s24") -> R.drawable.ic_product_phone
        cat.contains("audio") || t.contains("headphone") || t.contains("wh-1000xm5") || t.contains("earbuds") -> R.drawable.ic_product_audio
        cat.contains("fashion") || t.contains("sneaker") || t.contains("air jordan") || t.contains("shoe") -> R.drawable.ic_product_fashion
        cat.contains("appliance") || t.contains("airfryer") || t.contains("philips") || t.contains("cooker") -> R.drawable.ic_product_appliance
        cat.contains("quick") || cat.contains("grocery") || t.contains("almond") || t.contains("juice") || t.contains("trail") -> R.drawable.ic_product_grocery
        cat.contains("electronic") -> R.drawable.ic_product_laptop
        else -> R.drawable.ic_product_default
    }
}

@Composable
fun ProductImage(
    imageUrl: String?,
    categoryName: String? = null,
    title: String? = null,
    brandName: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    category: String? = null
) {
    val effectiveCat = categoryName ?: category
    val fallbackRes = getProductDrawableRes(effectiveCat, title, brandName)
    val context = LocalContext.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Image(
                painter = painterResource(id = fallbackRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}
