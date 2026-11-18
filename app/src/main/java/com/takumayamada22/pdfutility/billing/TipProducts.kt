package com.takumayamada22.pdfutility.billing

enum class TipTier(val productId: String, val badge: String) {
    BRONZE("tip_100", "🥉"), 
    SILVER("tip_500", "🥈"), 
    GOLD("tip_1000", "🥇");
    
    companion object { 
        fun fromProductId(id: String) = entries.firstOrNull { it.productId == id } 
    }
}
