package com.aukdeshop.notifications.server

data class FCMBody ( val to: String = "",
                     val priority: String = "",
                     var data: Map<String, String>
)

